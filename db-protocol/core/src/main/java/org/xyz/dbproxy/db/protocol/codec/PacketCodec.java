package org.xyz.dbproxy.db.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.db.protocol.packet.DatabasePacket;

import java.util.List;

/**
 * Database packet codec.
 */
@RequiredArgsConstructor
@Slf4j
public final class PacketCodec extends ByteToMessageCodec<DatabasePacket<?>> {
    private final DatabasePacketCodecEngine databasePacketCodecEngine;

    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) {
        int readableBytes = in.readableBytes();
        if (!databasePacketCodecEngine.isValidHeader(readableBytes)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Read from client {} :\n{}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(in));
        }
        databasePacketCodecEngine.decode(context, in, out);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void encode(final ChannelHandlerContext context, final DatabasePacket<?> message, final ByteBuf out) {
        databasePacketCodecEngine.encode(context, message, out);
        if (log.isDebugEnabled()) {
            log.debug("Write to client {} :\n{}", context.channel().id().asShortText(), ByteBufUtil.prettyHexDump(out));
        }
    }
}
