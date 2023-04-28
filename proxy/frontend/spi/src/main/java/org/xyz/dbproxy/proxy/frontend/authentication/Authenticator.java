package org.xyz.dbproxy.proxy.frontend.authentication;

import org.xyz.dbproxy.db.protocol.constant.AuthenticationMethod;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;

/**
 * Authenticator.
 */
public interface Authenticator {

    /**
     * Authenticate.
     *
     * @param user ShardingSphere user
     * @param authInfo authentication information
     * @return authentication success or not
     */
    boolean authenticate(DbProxyUser user, Object[] authInfo);

    /**
     * Get authentication method.
     *
     * @return authentication method
     */
    AuthenticationMethod getAuthenticationMethod();
}
