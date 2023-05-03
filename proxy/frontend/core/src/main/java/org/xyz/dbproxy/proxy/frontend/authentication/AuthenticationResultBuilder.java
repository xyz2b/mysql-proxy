package org.xyz.dbproxy.proxy.frontend.authentication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Authentication result builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticationResultBuilder {

    /**
     * Create finished authentication result.
     *
     * @param username username
     * @param hostname hostname
     * @param database database
     * @return finished authentication result
     */
    public static AuthenticationResult finished(final String username, final String hostname, final String database) {
        return new AuthenticationResult(username, hostname, database, true);
    }

    /**
     * Create continued authentication result.
     *
     * @return continued authentication result
     */
    public static AuthenticationResult continued() {
        return new AuthenticationResult(null, null, null, false);
    }

    /**
     * Create continued authentication result.
     *
     * @param username username
     * @param hostname hostname
     * @param database database
     * @return continued authentication result
     */
    public static AuthenticationResult continued(final String username, final String hostname, final String database) {
        return new AuthenticationResult(username, hostname, database, false);
    }
}