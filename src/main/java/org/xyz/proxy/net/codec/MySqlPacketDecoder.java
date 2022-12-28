package org.xyz.proxy.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.net.exception.InvalidFrameException;
import org.xyz.proxy.net.proto.mysql.BinaryPacket;
import org.xyz.proxy.net.util.ByteReaderUtil;

import java.nio.ByteOrder;

@Slf4j
public class MySqlPacketDecoder extends LengthFieldBasedFrameDecoder  {
    private static final int maxPacketSize = 16 * 1024 * 1024;

    public MySqlPacketDecoder() {
        // 注意mysql协议是小端字节序
        super(ByteOrder.LITTLE_ENDIAN, maxPacketSize, 0, 3, 1, 0, true);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        int payloadLength = ByteReaderUtil.readUB3(frame);
        // 长度如果小于0
        if (payloadLength < 0) {
            throw new InvalidFrameException(String.format("get packet sequenceId error, packetLength = %d", payloadLength));
        }

        int sequenceId = ByteReaderUtil.readUB1(frame);
        // sequence_id如果小于0
        if (sequenceId < 0) {
            throw new InvalidFrameException(String.format("get packet sequenceId error, packetLength = %d, packetSequenceId = %d", payloadLength, sequenceId));
        }
        BinaryPacket packet = new BinaryPacket();
        packet.setPayloadLength(payloadLength);
        packet.setSequenceId(sequenceId);
        // data will not be accessed any more,so we can use this array safely
        packet.payload = ByteReaderUtil.readBytes(frame, payloadLength);
        if (packet.payload == null || packet.payload.readableBytes() == 0) {
            throw new InvalidFrameException(String.format("get packet payload error, packetLength = %d, packetSequenceId = %d", packet.getPayloadLength(), packet.getSequenceId()));
        }

        return packet;
    }
}
