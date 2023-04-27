package org.xyz.dbproxy.infra.util.exception.external.server;

import org.xyz.dbproxy.infra.util.exception.external.ProxyExternalException;

/**
 * DbProxy server exception.
 */
public abstract class DbProxyServerException extends ProxyExternalException {

    private static final long serialVersionUID = 1547233217081261239L;

    public DbProxyServerException(final String errorCategory, final int errorCode, final String message) {
        super(String.format("%s-%05d: %s", errorCategory, errorCode, message));
    }

    public DbProxyServerException(final String errorCategory, final int errorCode, final String message, final Exception cause) {
        super(String.format("%s-%05d: %s", errorCategory, errorCode, message), cause);
    }
}

