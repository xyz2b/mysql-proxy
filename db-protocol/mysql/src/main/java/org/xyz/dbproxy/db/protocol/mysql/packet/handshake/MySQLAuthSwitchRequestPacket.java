package org.xyz.dbproxy.db.protocol.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.Arrays;

/**
 * MySQL authentication switch request packet.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_auth_switch_request.html">AuthSwitchRequest</a>
 */
@RequiredArgsConstructor
public final class MySQLAuthSwitchRequestPacket implements MySQLPacket {

    /**
     * Header of MySQL auth switch request packet.
     */
    public static final int HEADER = 0xfe;

    @Getter
    private final String authPluginName;

    @Getter
    private final MySQLAuthenticationPluginData authPluginData;

    public MySQLAuthSwitchRequestPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL auth switch request packet must be `0xfe`.");
        authPluginName = payload.readStringNul();
        String strAuthPluginData = payload.readStringNul();
        authPluginData = new MySQLAuthenticationPluginData(Arrays.copyOfRange(strAuthPluginData.getBytes(), 0, 8),
                Arrays.copyOfRange(strAuthPluginData.getBytes(), 8, 20));
    }

    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeStringNul(authPluginName);
        payload.writeStringNul(new String(authPluginData.getAuthenticationPluginData()));
    }
}