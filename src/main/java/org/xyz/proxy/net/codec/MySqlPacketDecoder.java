package org.xyz.proxy.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.springframework.stereotype.Service;
import org.xyz.proxy.net.exception.InvalidFrameException;
import org.xyz.proxy.net.proto.mysql.BinaryPacket;
import org.xyz.proxy.net.proto.util.ByteReaderUtil;
import org.xyz.proxy.util.GsonUtil;

@Slf4j
@Service("MySqlPacketDecoder")
public class MySqlPacketDecoder extends LengthFieldBasedFrameDecoder  {
    private static final int maxPacketSize = 16 * 1024 * 1024;

    public MySqlPacketDecoder() {
        super(maxPacketSize, 0, 3, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        int payloadLength = ByteReaderUtil.readUB3(in);
        // 长度如果小于0
        if (payloadLength < 0) {
            throw new InvalidFrameException(String.format("get packet sequenceId error, packetLength = %d", payloadLength));
        }

        byte sequenceId = in.readByte();
        // sequence_id如果小于0
        if (sequenceId < 0) {
            throw new InvalidFrameException(String.format("get packet sequenceId error, packetLength = %d, packetSequenceId = %d", payloadLength, sequenceId));
        }
        BinaryPacket packet = new BinaryPacket();
        packet.setPayloadLength(payloadLength);
        packet.setSequenceId(sequenceId);
        // data will not be accessed any more,so we can use this array safely
        packet.payload = in.readBytes(payloadLength).array();
        if (packet.payload == null || packet.payload.length == 0) {
            throw new InvalidFrameException(String.format("get packet payload error, packetLength = %d, packetSequenceId = %d", packet.getPayloadLength(), packet.getSequenceId()));
        }

        log.debug(GsonUtil.pojoToJson(packet));
        return packet;
    }
}
