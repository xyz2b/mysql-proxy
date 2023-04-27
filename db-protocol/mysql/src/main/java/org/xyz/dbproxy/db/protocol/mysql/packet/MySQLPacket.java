package org.xyz.dbproxy.db.protocol.mysql.packet;

import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;
import org.xyz.dbproxy.db.protocol.packet.DatabasePacket;

/**
 * Database packet for MySQL.
 */
public interface MySQLPacket extends DatabasePacket<MySQLPacketPayload> {
}

