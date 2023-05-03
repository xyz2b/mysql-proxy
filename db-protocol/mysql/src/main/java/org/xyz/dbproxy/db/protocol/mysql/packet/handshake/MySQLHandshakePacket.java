package org.xyz.dbproxy.db.protocol.mysql.packet.handshake;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLServerInfo;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLStatusFlag;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Handshake packet protocol for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_packets_protocol_handshake_v10.html">Handshake</a>
 */
@Getter
public final class MySQLHandshakePacket implements MySQLPacket {

    private final int protocolVersion = MySQLServerInfo.PROTOCOL_VERSION;

    private final String serverVersion;

    private final int connectionId;

    private final int capabilityFlagsLower;

    private final int characterSet;

    private final MySQLStatusFlag statusFlag;

    private final MySQLAuthenticationPluginData authPluginData;

    private int capabilityFlagsUpper;

    private String authPluginName;

    public MySQLHandshakePacket(final int connectionId, final MySQLAuthenticationPluginData authPluginData) {
        serverVersion = MySQLServerInfo.getDefaultServerVersion();
        this.connectionId = connectionId;
        capabilityFlagsLower = MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsLower();
        characterSet = MySQLServerInfo.DEFAULT_CHARSET.getId();
        statusFlag = MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT;
        capabilityFlagsUpper = MySQLCapabilityFlag.calculateHandshakeCapabilityFlagsUpper();
        this.authPluginData = authPluginData;
        authPluginName = MySQLAuthenticationMethod.NATIVE.getMethodName();
    }

    public MySQLHandshakePacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(protocolVersion == payload.readInt1());
        serverVersion = payload.readStringNul();
        connectionId = payload.readInt4();
        final byte[] authPluginDataPart1 = payload.readStringNulByBytes();
        capabilityFlagsLower = payload.readInt2();
        characterSet = payload.readInt1();
        statusFlag = MySQLStatusFlag.valueOf(payload.readInt2());
        capabilityFlagsUpper = payload.readInt2();
        payload.readInt1();
        payload.skipReserved(10);
        authPluginData = new MySQLAuthenticationPluginData(authPluginDataPart1, readAuthPluginDataPart2(payload));
        authPluginName = readAuthPluginName(payload);
    }

    /**
     * There are some different between implement of handshake initialization packet and document.
     * In source code of 5.7 version, authPluginDataPart2 should be at least 12 bytes,
     * and then follow a nul byte.
     * But in document, authPluginDataPart2 is at least 13 bytes, and not nul byte.
     * From test, the 13th byte is nul byte and should be excluded from authPluginDataPart2.
     *
     * @param payload MySQL packet payload
     * @return auth plugin data part2
     */
    private byte[] readAuthPluginDataPart2(final MySQLPacketPayload payload) {
        return isClientSecureConnection() ? payload.readStringNulByBytes() : new byte[0];
    }

    private String readAuthPluginName(final MySQLPacketPayload payload) {
        return isClientPluginAuth() ? payload.readStringNul() : null;
    }

    /**
     * Set authentication plugin name.
     *
     * @param authenticationMethod MySQL authentication method
     */
    public void setAuthPluginName(final MySQLAuthenticationMethod authenticationMethod) {
        authPluginName = authenticationMethod.getMethodName();
        capabilityFlagsUpper |= MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16;
    }

    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(protocolVersion);
        payload.writeStringNul(serverVersion);
        payload.writeInt4(connectionId);
        payload.writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart1()));
        payload.writeInt2(capabilityFlagsLower);
        payload.writeInt1(characterSet);
        payload.writeInt2(statusFlag.getValue());
        payload.writeInt2(capabilityFlagsUpper);
        payload.writeInt1(isClientPluginAuth() ? authPluginData.getAuthenticationPluginData().length + 1 : 0);
        payload.writeReserved(10);
        writeAuthPluginDataPart2(payload);
        writeAuthPluginName(payload);
    }

    private void writeAuthPluginDataPart2(final MySQLPacketPayload payload) {
        if (isClientSecureConnection()) {
            payload.writeStringNul(new String(authPluginData.getAuthenticationPluginDataPart2()));
        }
    }

    private void writeAuthPluginName(final MySQLPacketPayload payload) {
        if (isClientPluginAuth()) {
            payload.writeStringNul(authPluginName);
        }
    }

    private boolean isClientSecureConnection() {
        return 0 != (capabilityFlagsLower & MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION.getValue() & 0x00000ffff);
    }

    private boolean isClientPluginAuth() {
        return 0 != (capabilityFlagsUpper & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue() >> 16);
    }
}