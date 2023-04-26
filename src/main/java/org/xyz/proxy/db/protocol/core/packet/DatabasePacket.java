package org.xyz.proxy.db.protocol.core.packet;

import org.xyz.proxy.db.protocol.core.payload.PacketPayload;

/**
 * Database packet.
 *
 * @param <T> type of packet payload
 */
public interface DatabasePacket<T extends PacketPayload> {

    /**
     * Write packet to byte buffer.
     *
     * @param payload packet payload to be written
     */
    void write(T payload);
}
