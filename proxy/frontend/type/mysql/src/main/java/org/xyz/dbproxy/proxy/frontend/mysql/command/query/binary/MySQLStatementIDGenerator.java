package org.xyz.dbproxy.proxy.frontend.mysql.command.query.binary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statement ID generator for MySQL.
 * MYSQL 语句 ID生成器，一个连接关联一个
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class MySQLStatementIDGenerator {

    private static final MySQLStatementIDGenerator INSTANCE = new MySQLStatementIDGenerator();

    private final Map<Integer, AtomicInteger> connectionRegistry = new ConcurrentHashMap<>();

    /**
     * Get prepared statement registry instance.
     *
     * @return prepared statement registry instance
     */
    public static MySQLStatementIDGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Register connection.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        connectionRegistry.put(connectionId, new AtomicInteger());
    }

    /**
     * Generate next statement ID for connection.
     *
     * @param connectionId connection ID
     * @return generated statement ID for prepared statement
     */
    public int nextStatementId(final int connectionId) {
        return connectionRegistry.get(connectionId).incrementAndGet();
    }

    /**
     * Unregister connection.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        connectionRegistry.remove(connectionId);
    }
}
