package org.xyz.proxy.db.protocol.mysql.packet;

import org.xyz.proxy.db.protocol.core.packet.DatabasePacket;
import org.xyz.proxy.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Database packet for MySQL.
 */
public interface MySQLPacket extends DatabasePacket<MySQLPacketPayload> {
}

