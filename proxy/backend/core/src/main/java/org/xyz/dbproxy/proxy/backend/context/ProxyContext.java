package org.xyz.dbproxy.proxy.backend.context;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.mode.manager.ContextManager;
import org.xyz.dbproxy.proxy.backend.connector.jdbc.datasource.JDBCBackendDataSource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Proxy context.
 * 全局的context
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ProxyContext {

    private static final ProxyContext INSTANCE = new ProxyContext();

    private final JDBCBackendDataSource backendDataSource = new JDBCBackendDataSource();

    private ContextManager contextManager;

    /**
     * Initialize proxy context.
     *
     * @param contextManager context manager
     */
    public static void init(final ContextManager contextManager) {
        INSTANCE.contextManager = contextManager;
    }

    /**
     * Get instance of proxy context.
     *
     * @return got instance
     */
    public static ProxyContext getInstance() {
        return INSTANCE;
    }

    /**
     * Check database exists.
     *
     * @param name database name
     * @return database exists or not
     */
    public boolean databaseExists(final String name) {
        return contextManager.getMetaDataContexts().getMetaData().containsDatabase(name);
    }

    /**
     * Get database.
     *
     * @param name database name
     * @return got database
     */
    public ShardingSphereDatabase getDatabase(final String name) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(name) && contextManager.getMetaDataContexts().getMetaData().containsDatabase(name), NoDatabaseSelectedException::new);
        return contextManager.getMetaDataContexts().getMetaData().getDatabase(name);
    }

    /**
     * Get all database names.
     *
     * @return all database names
     */
    public Collection<String> getAllDatabaseNames() {
        return contextManager.getMetaDataContexts().getMetaData().getDatabases().values().stream().map(ShardingSphereDatabase::getName).collect(Collectors.toList());
    }

    /**
     * Get instance state context.
     *
     * @return instance state context
     */
    public Optional<InstanceStateContext> getInstanceStateContext() {
        return null == contextManager.getInstanceContext() ? Optional.empty() : Optional.ofNullable(contextManager.getInstanceContext().getInstance().getState());
    }
}
