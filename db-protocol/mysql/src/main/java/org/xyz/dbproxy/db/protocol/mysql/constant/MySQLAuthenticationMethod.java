package org.xyz.dbproxy.db.protocol.mysql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.db.protocol.constant.AuthenticationMethod;

/**
 * Authentication method for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods.html">Authentication Method</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLAuthenticationMethod implements AuthenticationMethod {

    OLD_PASSWORD("mysql_old_password"),

    NATIVE("mysql_native_password"),

    CLEAR_TEXT("mysql_clear_password"),

    WINDOWS_NATIVE("authentication_windows_client"),

    SHA256("sha256_password");

    private final String methodName;
}

