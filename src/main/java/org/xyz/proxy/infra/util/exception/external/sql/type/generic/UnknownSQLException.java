package org.xyz.proxy.infra.util.exception.external.sql.type.generic;

import org.xyz.proxy.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unknown SQL exception.
 */
public final class UnknownSQLException extends GenericSQLException {

    private static final long serialVersionUID = -7357918573504734977L;

    public UnknownSQLException(final Exception cause) {
        super(String.format("Unknown exception: %s", cause.getMessage()), cause, XOpenSQLState.GENERAL_ERROR, 0);
    }
}
