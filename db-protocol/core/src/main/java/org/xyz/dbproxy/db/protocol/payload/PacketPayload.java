package org.xyz.dbproxy.db.protocol.payload;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

/**
 * Packet payload.
 */
public interface PacketPayload extends AutoCloseable {

    /**
     * Get byte buf.
     *
     * @return byte buf
     */
    ByteBuf getByteBuf();

    /**
     * Get charset.
     *
     * @return charset
     */
    Charset getCharset();
}