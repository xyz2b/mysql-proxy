package org.xyz.dbproxy.db.protocol.mysql.packet.handshake;

import com.google.common.primitives.Bytes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Authentication plugin data for MySQL.
 *
 * <p>
 *     The auth-plugin-data is the concatenation of strings auth-plugin-data-part-1 and auth-plugin-data-part-2.
 *     The auth-plugin-data-part-1's length is 8; The auth-plugin-data-part-2's length is 12.
 * </p>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLAuthenticationPluginData {

    private final byte[] authenticationPluginDataPart1;

    private final byte[] authenticationPluginDataPart2;

    public MySQLAuthenticationPluginData() {
        this(MySQLRandomGenerator.getINSTANCE().generateRandomBytes(8), MySQLRandomGenerator.getINSTANCE().generateRandomBytes(12));
    }

    /**
     * Get authentication plugin data.
     *
     * @return authentication plugin data
     */
    public byte[] getAuthenticationPluginData() {
        return Bytes.concat(authenticationPluginDataPart1, authenticationPluginDataPart2);
    }
}