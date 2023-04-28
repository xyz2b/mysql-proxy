package org.xyz.dbproxy.proxy.frontend.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Connection thread executor group.
 * 每一个连接关联一个线程，这个线程有无限大的任务队列
 *
 * <p>
 * Manage the thread for each connection session invoking.
 * This ensure XA transaction framework processed by current thread id.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionThreadExecutorGroup {

    private static final ConnectionThreadExecutorGroup INSTANCE = new ConnectionThreadExecutorGroup();

    private final Map<Integer, ExecutorService> executorServices = new ConcurrentHashMap<>();

    /**
     * Get connection thread executor group.
     *
     * @return connection thread executor group
     */
    public static ConnectionThreadExecutorGroup getInstance() {
        return INSTANCE;
    }

    /**
     * Register connection.
     *
     * @param connectionId connection id
     */
    public void register(final int connectionId) {
        executorServices.put(connectionId, newSingleThreadExecutorService(connectionId));
    }

    private ExecutorService newSingleThreadExecutorService(final int connectionId) {
        return new ThreadPoolExecutor(0, 1, 1L, TimeUnit.HOURS, new LinkedBlockingQueue<>(), runnable -> new Thread(runnable, String.format("Connection-%d-ThreadExecutor", connectionId)));
    }

    /**
     * Get executor service of connection.
     *
     * @param connectionId connection id
     * @return executor service of current connection
     */
    public ExecutorService get(final int connectionId) {
        return executorServices.get(connectionId);
    }

    /**
     * Unregister connection and await termination.
     *
     * @param connectionId connection id
     */
    public void unregisterAndAwaitTermination(final int connectionId) {
        ExecutorService executorService = executorServices.remove(connectionId);
        if (null == executorService) {
            return;
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

