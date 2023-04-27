package org.xyz.dbproxy.infra.database.type;

import org.xyz.dbproxy.infra.util.spi.annotation.SingletonSPI;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Database type.
 */
@SingletonSPI
public interface DatabaseType extends TypedSPI {

    /**
     * Get quote character.
     *
     * @return quote character
     */
    QuoteCharacter getQuoteCharacter();

    /**
     * Get alias of JDBC URL prefixes.
     *
     * @return Alias of JDBC URL prefixes
     */
    Collection<String> getJdbcUrlPrefixes();

    /**
     * Get data source meta data.
     *
     * @param url URL of data source
     * @param username username of data source
     * @return data source meta data
     */
    DataSourceMetaData getDataSourceMetaData(String url, String username);

    /**
     * Get system database schema map.
     *
     * @return system database schema map
     */
    Map<String, Collection<String>> getSystemDatabaseSchemaMap();

    /**
     * Get system schemas.
     *
     * @return system schemas
     */
    Collection<String> getSystemSchemas();

    /**
     * Is schema feature available.
     *
     * @return true or false
     */
    default boolean isSchemaAvailable() {
        return false;
    }

    /**
     * Get schema.
     *
     * @param connection connection
     * @return schema
     */
    @SuppressWarnings("ReturnOfNull")
    default String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ignored) {
            return null;
        }
    }

    /**
     * Format table name pattern.
     *
     * @param tableNamePattern table name pattern
     * @return formatted table name pattern
     */
    default String formatTableNamePattern(final String tableNamePattern) {
        return tableNamePattern;
    }

    /**
     * Handle rollback only.
     *
     * @param rollbackOnly rollback only
     * @param statement statement
     * @throws SQLException SQL exception
     */
    default void handleRollbackOnly(final boolean rollbackOnly, final SQLStatement statement) throws SQLException {
    }
}
