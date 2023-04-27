package org.xyz.proxy.db.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.xyz.proxy.db.protocol.core.codec.DatabasePacketCodecEngine;
import org.xyz.proxy.db.protocol.core.constant.CommonConstants;
import org.xyz.proxy.db.protocol.core.packet.generic.MySQLErrPacket;
import org.xyz.proxy.db.protocol.core.payload.PacketPayload;
import org.xyz.proxy.db.protocol.mysql.constant.MySQLConstants;
import org.xyz.proxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.proxy.db.protocol.mysql.payload.MySQLPacketPayload;
import org.xyz.proxy.infra.util.exception.external.sql.type.generic.UnknownSQLException;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Database packet codec for MySQL.
 */
public final class MySQLPacketCodecEngine implements DatabasePacketCodecEngine<MySQLPacket> {
    /**
     * 如果MYSQL报文的payload length大于等于0xFFFFFF(2^24-1)，需要特殊处理
     * 如果一条消息的payload length大于等于0xFFFFFF，则将第一个MYSQL报文的payload_length设置为0xFFFFFF，
     * 后续MYSQL报文直到payload length小于0xFFFFFF(包含)之间的报文的payload都是这一条消息的
     */
    private static final int MAX_PAYLOAD_LENGTH = 0xFFFFFF;

    private static final int PAYLOAD_LENGTH = 3;

    private static final int SEQUENCE_LENGTH = 1;

    private final List<ByteBuf> pendingMessages = new LinkedList<>();

    @Override
    public boolean isValidHeader(int readableBytes) {
        return readableBytes >= PAYLOAD_LENGTH + SEQUENCE_LENGTH;
    }

    @Override
    public void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        // 读取三个字节（小端）
        int payloadLength = in.markReaderIndex().readUnsignedMediumLE();
        int remainPayloadLength = SEQUENCE_LENGTH + payloadLength;

        // 如果可读字节不足一个完整的报文，表示遇到半包，重置读index返回，继续接收后续报文
        if (in.readableBytes() < remainPayloadLength) {
            in.resetReaderIndex();
            return;
        }

        ByteBuf message = in.readRetainedSlice(remainPayloadLength);
        // 遇到payload length等于0xFFFFFF，先将其缓存起来，因为后续还有报文的payload是属于该条消息的（直到payload length小于0xFFFFFF(包含)的报文之间的报文都是属于这条消息的），等到接收完整条消息的所有报文再进行组装。
        // 此时并没有解码成功，所以并没有往out列表中放入解码后的消息。根据netty的逻辑：如果用户解码器从in中读取了数据，但是还没解码出来对象，说明可以解码，可以继续进行解码操作。netty会继续调用用户解码器进行解码
        if (MAX_PAYLOAD_LENGTH == payloadLength) {
            pendingMessages.add(message.skipBytes(SEQUENCE_LENGTH));
        } else if (pendingMessages.isEmpty()) {
            // 解码成功，将解码结果放到out中，返回
            out.add(message);
        } else {    // 直到遇到payload length小于0xFFFFFF的报文，说明一条消息的所有报文都接收完了，此时开始组装这条消息
            aggregateMessages(context, message, out);
        }
    }

    // 组装payload大于等于0xFFFFFF的消息
    private void aggregateMessages(final ChannelHandlerContext context, final ByteBuf lastMessage, final List<Object> out) {
        // 使用CompositeByteBuf，将多个ByteBuf组装在一起，零拷贝
        // +1是因为要包含最后payload length小于0xFFFFFF的报文
        CompositeByteBuf result = context.alloc().compositeBuffer(SEQUENCE_LENGTH + pendingMessages.size() + 1);
        result.addComponent(true, lastMessage.readSlice(SEQUENCE_LENGTH));
        Iterator<ByteBuf> pendingMessagesIterator = pendingMessages.iterator();
        result.addComponent(true, pendingMessagesIterator.next());
        while (pendingMessagesIterator.hasNext()) {
            result.addComponent(true, pendingMessagesIterator.next());
        }
        if (lastMessage.readableBytes() > 0) {
            result.addComponent(true, lastMessage);
        }
        out.add(result);
        pendingMessages.clear();
    }


    @Override
    public void encode(final ChannelHandlerContext context, final MySQLPacket message, final ByteBuf out) {
        // 创建MySQLPacketPayload， 传入接收编码后消息的ByteBuf，再传入从当前channel中取出之前建立channel时设置的charset属性
        // 注意创建MySQLPacketPayload接收的BytBuf，写入其中的编码后的内容只包含payload，不包含payload_length以及sequence_id
        // 所以这里将接收编码后消息的ByteBuf传入时，先用0占位符将payload_length以及sequence_id字段占位了，之后再写入对应字段的值
        MySQLPacketPayload payload = new MySQLPacketPayload(prepareMessageHeader(out).markWriterIndex(), context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            // 将message消息（payload）编码后写入payload的ByteBuf中（payload中的ByteBuf就是out）
            message.write(payload);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {  // 这里错误为什么会发生？
            // CHECKSTYLE:ON
            out.resetWriterIndex();
            SQLException unknownSQLException = new UnknownSQLException(ex).toSQLException();
            new MySQLErrPacket(unknownSQLException.getErrorCode(), unknownSQLException.getSQLState(), unknownSQLException.getMessage()).write(payload);
        } finally {
            // out ByteBuf存放了编码后的消息，包含payload_length、sequence_id以及payload
            // 如果编码后的payload小于0xFFFFFF，则一个报文即可容纳
            if (out.readableBytes() - PAYLOAD_LENGTH - SEQUENCE_LENGTH < MAX_PAYLOAD_LENGTH) {
                // 更新上面占位的payload_length以及sequence_id的字段
                updateMessageHeader(out, context.channel().attr(MySQLConstants.MYSQL_SEQUENCE_ID).get().getAndIncrement());
            } else {
                // 如果编码后的payload大于等于0xFFFFFF，需要拆成多个报文发送
                writeMultiPackets(context, out);
            }
        }
    }

    private ByteBuf prepareMessageHeader(final ByteBuf out) {
        return out.writeInt(0);
    }

    private void updateMessageHeader(final ByteBuf byteBuf, final int sequenceId) {
        // ByteBuf set不会更新writeIndex
        byteBuf.setMediumLE(0, byteBuf.readableBytes() - PAYLOAD_LENGTH - SEQUENCE_LENGTH);
        byteBuf.setByte(3, sequenceId);
    }

    private void writeMultiPackets(final ChannelHandlerContext context, final ByteBuf byteBuf) {
        // 计算需要拆成多少个报文。这里多做了一个操作，将之前写入out ByteBuf的payload_length以及sequence_id占位符给跳过
        int packetCount = byteBuf.skipBytes(PAYLOAD_LENGTH + SEQUENCE_LENGTH).readableBytes() / MAX_PAYLOAD_LENGTH + 1;
        // 为啥要申请报文数*2的CompositeByteBuf，因为一个报文分为报文头(payload_length+sequence_id)和payload，所以一个报文需要两个ByteBuf来保存
        CompositeByteBuf result = context.alloc().compositeBuffer(packetCount * 2);
        // 获取当前channel最新的sequenceId
        AtomicInteger sequenceId = context.channel().attr(MySQLConstants.MYSQL_SEQUENCE_ID).get();
        for (int i = 0; i < packetCount; i++) {
            // 申请报文头的ByteBuf，4字节。然后写入payload_length以及sequence_id
            ByteBuf header = context.alloc().ioBuffer(4, 4);
            int packetLength = Math.min(byteBuf.readableBytes(), MAX_PAYLOAD_LENGTH);
            header.writeMediumLE(packetLength);
            header.writeByte(sequenceId.getAndIncrement());
            // 将报文头ByteBuf写入到CompositeByteBuf
            result.addComponent(true, header);
            if (packetLength > 0) { // 如果payload大于0，将payload写入CompositeByteBuf
                // readRetainedSlice，从ByteBuf当前readIndex处开始截出packetLength长度的分片
                result.addComponent(true, byteBuf.readRetainedSlice(packetLength));
            }
        }
        // 然后将存放多个报文的CompositeByteBuf，写入channel中
        context.write(result);
    }

    @Override
    public PacketPayload createPacketPayload(ByteBuf message, Charset charset) {
        return new MySQLPacketPayload(message, charset);
    }
}
