package org.xyz.dbproxy.proxy.frontend.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.infra.database.type.DatabaseType;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;

import java.util.Optional;

/**
 * Front database protocol type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FrontDatabaseProtocolTypeFactory {

    private static final String DEFAULT_FRONTEND_DATABASE_PROTOCOL_TYPE = "MySQL";

    /**
     * Get front database protocol type.
     *
     * @return front database protocol type
     */
    public static DatabaseType getDatabaseType() {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType();
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        if (metaDataContexts.getMetaData().getDatabases().isEmpty()) {
            return DatabaseTypeEngine.getTrunkDatabaseType(DEFAULT_FRONTEND_DATABASE_PROTOCOL_TYPE);
        }
        Optional<ShardingSphereDatabase> database = metaDataContexts.getMetaData().getDatabases().values().stream().filter(ShardingSphereDatabase::containsDataSource).findFirst();
        return database.isPresent() ? database.get().getResourceMetaData().getStorageTypes().values().iterator().next()
                : DatabaseTypeEngine.getTrunkDatabaseType(DEFAULT_FRONTEND_DATABASE_PROTOCOL_TYPE);
    }

    private static Optional<DatabaseType> findConfiguredDatabaseType() {
        String configuredDatabaseType = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return configuredDatabaseType.isEmpty() ? Optional.empty() : Optional.of(DatabaseTypeEngine.getTrunkDatabaseType(configuredDatabaseType));
    }
}
