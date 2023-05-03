package org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.impl;

import org.apache.commons.codec.digest.DigestUtils;
import com.google.common.base.Strings;
import org.xyz.dbproxy.db.protocol.constant.AuthenticationMethod;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.xyz.dbproxy.db.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;

import java.util.Arrays;

/**
 * Native password authenticator for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_native_password_authentication.html">Native Password Authentication</a>
 */
public final class MySQLNativePasswordAuthenticator implements MySQLAuthenticator {

    @Override
    public boolean authenticate(final DbProxyUser user, final Object[] authInfo) {
        byte[] authResponse = (byte[]) authInfo[0];
        MySQLAuthenticationPluginData authPluginData = (MySQLAuthenticationPluginData) authInfo[1];
        return Strings.isNullOrEmpty(user.getPassword()) || Arrays.equals(getAuthCipherBytes(user.getPassword(), authPluginData.getAuthenticationPluginData()), authResponse);
    }

    private byte[] getAuthCipherBytes(final String password, final byte[] authenticationPluginData) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[authenticationPluginData.length + doubleSha1Password.length];
        System.arraycopy(authenticationPluginData, 0, concatBytes, 0, authenticationPluginData.length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, authenticationPluginData.length, doubleSha1Password.length);
        byte[] sha1ConcatBytes = DigestUtils.sha1(concatBytes);
        return xor(sha1Password, sha1ConcatBytes);
    }

    private byte[] xor(final byte[] input, final byte[] secret) {
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            result[i] = (byte) (input[i] ^ secret[i]);
        }
        return result;
    }

    @Override
    public AuthenticationMethod getAuthenticationMethod() {
        return MySQLAuthenticationMethod.NATIVE;
    }
}