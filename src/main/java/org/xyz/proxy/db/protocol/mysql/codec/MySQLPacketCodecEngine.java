package org.xyz.proxy.db.protocol.mysql.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.xyz.proxy.db.protocol.core.codec.DatabasePacketCodecEngine;
import org.xyz.proxy.db.protocol.core.payload.PacketPayload;
import org.xyz.proxy.db.protocol.mysql.packet.MySQLPacket;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Database packet codec for MySQL.
 */
public final class MySQLPacketCodecEngine implements DatabasePacketCodecEngine<MySQLPacket> {

    @Override
    public boolean isValidHeader(int readableBytes) {
        return false;
    }

    @Override
    public void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {

    }

    @Override
    public void encode(ChannelHandlerContext context, MySQLPacket message, ByteBuf out) {

    }

    @Override
    public PacketPayload createPacketPayload(ByteBuf message, Charset charset) {
        return null;
    }
}
