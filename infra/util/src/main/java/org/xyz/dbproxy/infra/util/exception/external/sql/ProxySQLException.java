package org.xyz.dbproxy.infra.util.exception.external.sql;

import org.xyz.dbproxy.infra.util.exception.external.ProxyExternalException;
import org.xyz.dbproxy.infra.util.exception.external.sql.sqlstate.SQLState;

import java.sql.SQLException;

/**
 * Proxy SQL exception.
 */
public abstract class ProxySQLException extends ProxyExternalException {

    private static final long serialVersionUID = -8238061892944243621L;

    private final String sqlState;

    private final int vendorCode;

    private final String reason;

    private final Exception cause;

    public ProxySQLException(final SQLState sqlState, final int typeOffset, final int errorCode, final String reason, final Object... messageArgs) {
        this(sqlState.getValue(), typeOffset, errorCode, reason, messageArgs);
    }

    public ProxySQLException(final String sqlState, final int typeOffset, final int errorCode, final String reason, final Object... messageArgs) {
        this(sqlState, typeOffset, errorCode, null == reason ? null : String.format(reason, messageArgs), (Exception) null);
    }

    public ProxySQLException(final String sqlState, final int typeOffset, final int errorCode, final String reason, final Exception cause) {
        super(reason, cause);
        this.sqlState = sqlState;
        vendorCode = typeOffset * 10000 + errorCode;
        this.reason = reason;
        this.cause = cause;
    }

    /**
     * To SQL exception.
     *
     * @return SQL exception
     */
    public final SQLException toSQLException() {
        return new SQLException(reason, sqlState, vendorCode, cause);
    }
}
