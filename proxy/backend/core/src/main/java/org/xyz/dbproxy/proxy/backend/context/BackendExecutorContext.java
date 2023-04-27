package org.xyz.dbproxy.proxy.backend.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Backend executor context.
 * 后端执行器的context
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class BackendExecutorContext {

    private static final BackendExecutorContext INSTANCE = new BackendExecutorContext();

    private final ExecutorEngine executorEngine = ExecutorEngine.createExecutorEngineWithSize(
            ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));

    /**
     * Get executor context instance.
     *
     * @return instance of executor context
     */
    public static BackendExecutorContext getInstance() {
        return INSTANCE;
    }
}