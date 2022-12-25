package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.xyz.proxy.net.proto.util.ByteWriterUtil;

// MySql包 外层结构
public class BinaryPacket extends MySQLPacket {
    public static final byte OK = 1;
    public static final byte ERROR = 2;
    public static final byte HEADER = 3;
    public static final byte FIELD = 4;
    public static final byte FIELD_EOF = 5;
    public static final byte ROW = 6;
    public static final byte PACKET_EOF = 7;

    public byte[] payload;

    @Override
    public int calcPacketSize() {
        return payload == null ? 0 : payload.length;
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(byteBuf, getPayloadLength());
        byteBuf.writeByte(getSequenceId());
        byteBuf.writeBytes(payload);
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Binary Packet";
    }
}
