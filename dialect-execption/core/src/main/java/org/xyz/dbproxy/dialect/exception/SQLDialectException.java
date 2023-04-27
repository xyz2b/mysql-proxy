package org.xyz.dbproxy.dialect.exception;

import lombok.NoArgsConstructor;
import org.xyz.dbproxy.infra.util.exception.external.ProxyExternalException;

/**
 * SQL dialect exception.
 */
@NoArgsConstructor
public abstract class SQLDialectException extends ProxyExternalException {

    private static final long serialVersionUID = -5090068160364259336L;

    public SQLDialectException(final String reason) {
        super(reason);
    }
}

