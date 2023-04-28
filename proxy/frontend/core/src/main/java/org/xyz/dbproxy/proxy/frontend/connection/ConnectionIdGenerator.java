package org.xyz.dbproxy.proxy.frontend.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Connection ID generator.、
 * thread id
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ConnectionIdGenerator {

    private static final ConnectionIdGenerator INSTANCE = new ConnectionIdGenerator();

    private int currentId;

    /**
     * Get instance.
     *
     * @return instance
     */
    public static ConnectionIdGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Get next connection ID.
     *
     * @return next connection ID
     */
    public synchronized int nextId() {
        if (currentId == Integer.MAX_VALUE) {
            currentId = 0;
        }
        return ++currentId;
    }
}
