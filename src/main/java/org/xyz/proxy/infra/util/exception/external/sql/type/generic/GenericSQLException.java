package org.xyz.proxy.infra.util.exception.external.sql.type.generic;

import org.xyz.proxy.infra.util.exception.external.sql.ProxySQLException;
import org.xyz.proxy.infra.util.exception.external.sql.sqlstate.SQLState;

/**
 * Generic SQL exception.
 */
public abstract class GenericSQLException extends ProxySQLException {

    private static final long serialVersionUID = 1156879276497567865L;

    private static final int TYPE_OFFSET = 3;

    public GenericSQLException(final SQLState sqlState, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, TYPE_OFFSET, errorCode, reason, messageArgs);
    }

    public GenericSQLException(final String reason, final Exception cause, final SQLState sqlState, final int errorCode) {
        super(sqlState.getValue(), TYPE_OFFSET, errorCode, reason, cause);
    }
}

