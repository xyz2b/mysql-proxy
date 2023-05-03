package org.xyz.dbproxy.db.protocol.mysql.packet.handshake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * MySQL authentication switch request packet.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_switch_response.html">AuthSwitchResponse</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLAuthSwitchResponsePacket implements MySQLPacket {

    private final byte[] authPluginResponse;

    public MySQLAuthSwitchResponsePacket(final MySQLPacketPayload payload) {
        authPluginResponse = payload.readStringEOFByBytes();
    }

    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeBytes(authPluginResponse);
    }
}

