package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.proto.util.ByteWriterUtil;

@Data
public class AuthSwitchRequest extends MySQLPacket {

    public static final byte PACKET_ID = (byte) 0xFE;

    private byte statusFlag = PACKET_ID;
    private String pluginName;
    private byte[] seed;    // plugin provided data，新的盐值

    public void read(BinaryPacket bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());

        MySQLMessageStream mm = new MySQLMessageStream(bin.payload);
        statusFlag = (byte) mm.readUB1();
        pluginName = mm.readStringWithNull();
        seed = mm.readBytesWithNull();
    }

    public void write(final ChannelHandlerContext ctx) {
        final ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

        ByteWriterUtil.writeBytesWithNull(buffer, pluginName.getBytes());
        ByteWriterUtil.writeBytesWithNull(buffer, seed);
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 1;   // status flag
        size += pluginName.length() + 1; // plugin name + null
        size += seed.length + 1;    // plugin provided data + null
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL AuthSwitchRequest Packet";
    }
}
