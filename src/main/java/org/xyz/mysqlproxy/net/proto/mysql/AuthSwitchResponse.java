package org.xyz.mysqlproxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.mysqlproxy.net.util.ByteWriterUtil;

@Data
public class AuthSwitchResponse extends MySQLPacketDef {
    private byte[] password; // data

    public void read(BinaryPacketDef bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());

        MySQLMessageStream mm = new MySQLMessageStream(bin.payload);
        password = mm.readBytes();
    }

    public void write(final ChannelHandlerContext ctx) {
        final ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

        ByteWriterUtil.writeBytes(buffer, password);
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        return password.length;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL AuthSwitchResponse Packet";
    }
}
