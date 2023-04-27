package org.xyz.dbproxy.db.protocol.packet;

import org.xyz.dbproxy.db.protocol.payload.PacketPayload;

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
