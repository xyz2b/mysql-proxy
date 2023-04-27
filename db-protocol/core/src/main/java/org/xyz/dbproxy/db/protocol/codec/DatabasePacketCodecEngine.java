package org.xyz.dbproxy.db.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;
import org.xyz.dbproxy.db.protocol.packet.DatabasePacket;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Database packet codec engine.
 *
 * @param <T> type of database packet
 */
public interface DatabasePacketCodecEngine<T extends DatabasePacket<?>> {

    /**
     * Judge is valid header or not.
     *
     * @param readableBytes readable bytes
     * @return is valid header or not
     */
    boolean isValidHeader(int readableBytes);

    /**
     * Decode.
     *
     * @param context channel handler context
     * @param in input
     * @param out output
     */
    void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out);

    /**
     * Encode.
     *
     * @param context channel handler context
     * @param message message of database packet
     * @param out output
     */
    void encode(ChannelHandlerContext context, T message, ByteBuf out);

    /**
     * Create packet payload.
     *
     * @param message message
     * @param charset charset
     * @return packet payload
     */
    PacketPayload createPacketPayload(ByteBuf message, Charset charset);
}
