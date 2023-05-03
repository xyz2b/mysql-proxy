package org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.proxy.frontend.authentication.AuthenticatorType;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.impl.MySQLClearPasswordAuthenticator;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.impl.MySQLNativePasswordAuthenticator;

/**
 * Authenticator type for MySQL.
 */
@RequiredArgsConstructor
@Getter
public enum MySQLAuthenticatorType implements AuthenticatorType {

    // TODO impl OLD_PASSWORD Authenticator
    OLD_PASSWORD(MySQLNativePasswordAuthenticator.class),

    NATIVE(MySQLNativePasswordAuthenticator.class, true),

    CLEAR_TEXT(MySQLClearPasswordAuthenticator.class),

    // TODO impl WINDOWS_NATIVE Authenticator
    WINDOWS_NATIVE(MySQLNativePasswordAuthenticator.class),

    // TODO impl SHA256 Authenticator
    SHA256(MySQLNativePasswordAuthenticator.class);

    private final Class<? extends MySQLAuthenticator> authenticatorClass;

    private final boolean isDefault;

    MySQLAuthenticatorType(final Class<? extends MySQLAuthenticator> authenticatorClass) {
        this(authenticatorClass, false);
    }
}