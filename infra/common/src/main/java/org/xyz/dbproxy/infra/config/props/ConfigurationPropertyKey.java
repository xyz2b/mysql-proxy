package org.xyz.dbproxy.infra.config.props;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.infra.util.props.TypedPropertyKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Typed property key of configuration.
 */
@RequiredArgsConstructor
@Getter
public enum ConfigurationPropertyKey implements TypedPropertyKey {

    /**
     * The system log level.
     */
    SYSTEM_LOG_LEVEL("system-log-level", LoggerLevel.INFO.name(), LoggerLevel.class, false),

    /**
     * Whether show SQL in log.
     */
    SQL_SHOW("sql-show", String.valueOf(Boolean.FALSE), boolean.class, false),

    /**
     * Whether show SQL details in simple style.
     */
    SQL_SIMPLE("sql-simple", String.valueOf(Boolean.FALSE), boolean.class, false),

    /**
     * The max thread size of worker group to execute SQL.
     */
    KERNEL_EXECUTOR_SIZE("kernel-executor-size", String.valueOf(0), int.class, true),

    /**
     * Max opened connection size for each query.
     */
    MAX_CONNECTIONS_SIZE_PER_QUERY("max-connections-size-per-query", String.valueOf(1), int.class, false),

    /**
     * Whether validate table meta data consistency when application startup or updated.
     */
    CHECK_TABLE_META_DATA_ENABLED("check-table-metadata-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),

    /**
     * SQL federation type.
     */
    SQL_FEDERATION_TYPE("sql-federation-type", "NONE", String.class, false),

    /**
     * Frontend database protocol type for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE("proxy-frontend-database-protocol-type", "", String.class, false),

    /**
     * Flush threshold for every records from databases for ShardingSphere-Proxy.
     */
    PROXY_FRONTEND_FLUSH_THRESHOLD("proxy-frontend-flush-threshold", String.valueOf(128), int.class, false),

    /**
     * Whether enable hint for ShardingSphere-Proxy.
     */
    PROXY_HINT_ENABLED("proxy-hint-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),

    /**
     * Proxy backend query fetch size. A larger value may increase the memory usage of ShardingSphere Proxy.
     * The default value is -1, which means set the minimum value for different JDBC drivers.
     */
    PROXY_BACKEND_QUERY_FETCH_SIZE("proxy-backend-query-fetch-size", String.valueOf(-1), int.class, false),

    /**
     * Proxy frontend executor size. The default value is 0, which means let Netty decide.
     */
    PROXY_FRONTEND_EXECUTOR_SIZE("proxy-frontend-executor-size", String.valueOf(0), int.class, true),

    /**
     * Less than or equal to 0 means no limitation.
     */
    PROXY_FRONTEND_MAX_CONNECTIONS("proxy-frontend-max-connections", "0", int.class, false),

    /**
     * Proxy default start port.
     */
    PROXY_DEFAULT_PORT("proxy-default-port", "3307", int.class, true),

    /**
     * Proxy Netty backlog size.
     */
    PROXY_NETTY_BACKLOG("proxy-netty-backlog", "1024", int.class, false),

    /**
     * Proxy instance type.
     */
    PROXY_INSTANCE_TYPE("proxy-instance-type", "Proxy", String.class, true),

    /**
     * CDC server port.
     */
    CDC_SERVER_PORT("cdc-server-port", "33071", int.class, true);

    private final String key;

    private final String defaultValue;

    private final Class<?> type;

    private final boolean rebootRequired;

    /**
     * Get property key names.
     *
     * @return collection of key names
     */
    public static Collection<String> getKeyNames() {
        return Arrays.stream(values()).map(ConfigurationPropertyKey::name).collect(Collectors.toList());
    }
}

