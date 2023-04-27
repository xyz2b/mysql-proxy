package org.xyz.dbproxy.infra.util.exception.external;

import lombok.NoArgsConstructor;

/**
 * Proxy external exception.
 */
@NoArgsConstructor
public abstract class ProxyExternalException extends RuntimeException {

    private static final long serialVersionUID = 1629786588176694067L;

    public ProxyExternalException(final String reason) {
        super(reason);
    }

    public ProxyExternalException(final String reason, final Exception cause) {
        super(reason, cause);
    }
}
