package org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.impl;

import com.google.common.base.Strings;
import org.xyz.dbproxy.db.protocol.constant.AuthenticationMethod;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;

/**
 * Clear password authenticator for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_clear_text_password.html">Clear Text Authentication</a>
 */
public final class MySQLClearPasswordAuthenticator implements MySQLAuthenticator {

    @Override
    public boolean authenticate(final DbProxyUser user, final Object[] authInfo) {
        byte[] authResponse = (byte[]) authInfo[0];
        byte[] password = new byte[authResponse.length - 1];
        System.arraycopy(authResponse, 0, password, 0, authResponse.length - 1);
        return Strings.isNullOrEmpty(user.getPassword()) || user.getPassword().equals(new String(password));
    }

    @Override
    public AuthenticationMethod getAuthenticationMethod() {
        return MySQLAuthenticationMethod.CLEAR_TEXT;
    }
}
