package org.xyz.dbproxy.proxy.frontend.authentication;

/**
 * Authenticator type.
 */
public interface AuthenticatorType {

    /**
     * Get authenticator class.
     *
     * @return authenticator class
     */
    Class<? extends Authenticator> getAuthenticatorClass();

    /**
     * Is default authenticator.
     *
     * @return is default authenticator
     */
    boolean isDefault();
}
