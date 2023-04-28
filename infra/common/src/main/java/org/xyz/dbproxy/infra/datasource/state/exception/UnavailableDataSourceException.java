package org.xyz.dbproxy.infra.datasource.state.exception;

import org.xyz.dbproxy.infra.util.exception.external.server.DbProxyServerException;

import java.sql.SQLException;

/**
 * Data source state exception.
 */
public final class UnavailableDataSourceException extends DbProxyServerException {

    private static final long serialVersionUID = -8058761885303180333L;

    private static final String ERROR_CATEGORY = "DATA-SOURCE";

    private static final int ERROR_CODE = 1;

    public UnavailableDataSourceException(final SQLException cause, final String dataSourceName) {
        super(ERROR_CATEGORY, ERROR_CODE, String.format("Data source `%s` is unavailable.", dataSourceName), cause);
    }
}
