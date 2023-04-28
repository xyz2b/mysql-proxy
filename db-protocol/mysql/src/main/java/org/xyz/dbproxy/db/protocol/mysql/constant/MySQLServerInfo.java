package org.xyz.dbproxy.db.protocol.mysql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.db.protocol.constant.CommonConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere-Proxy's information for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLServerInfo {

    /**
     * Protocol version is always 0x0A.
     */
    public static final int PROTOCOL_VERSION = 0x01;

    public static final MySQLCharacterSet DEFAULT_CHARSET = MySQLCharacterSet.UTF8MB4_GENERAL_CI;

    private static String defaultMysqlVersion = "5.7.22";

    private static final String SERVER_VERSION_PATTERN = "%s-DB-Proxy %s";

    private static final Map<String, String> SERVER_VERSIONS = new ConcurrentHashMap<>();

    /**
     * Set server version.
     *
     * @param databaseName database name
     * @param serverVersion server version
     */
    public static void setServerVersion(final String databaseName, final String serverVersion) {
        SERVER_VERSIONS.put(databaseName, String.format(SERVER_VERSION_PATTERN, serverVersion, CommonConstants.PROXY_VERSION.get()));
    }

    /**
     * Get current server version by schemaName.
     *
     * @param databaseName database name
     * @return server version
     */
    public static String getServerVersion(final String databaseName) {
        return null == databaseName ? getDefaultServerVersion() : SERVER_VERSIONS.getOrDefault(databaseName, getDefaultServerVersion());
    }

    /**
     * Set default mysql version.
     *
     * @param defaultMysqlVersion default mysql version
     */
    public static void setDefaultMysqlVersion(final String defaultMysqlVersion) {
        MySQLServerInfo.defaultMysqlVersion = defaultMysqlVersion;
    }

    /**
     * Get default server version.
     *
     * @return server version
     */
    public static String getDefaultServerVersion() {
        return String.format(SERVER_VERSION_PATTERN, defaultMysqlVersion, CommonConstants.PROXY_VERSION.get());
    }
}
