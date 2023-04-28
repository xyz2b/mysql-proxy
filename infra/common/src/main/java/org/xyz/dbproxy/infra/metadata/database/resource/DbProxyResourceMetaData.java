package org.xyz.dbproxy.infra.metadata.database.resource;

import lombok.AccessLevel;
import lombok.Getter;
import org.xyz.dbproxy.infra.database.metadata.DataSourceMetaData;
import org.xyz.dbproxy.infra.database.type.DatabaseType;
import org.xyz.dbproxy.infra.database.type.DatabaseTypeEngine;
import org.xyz.dbproxy.infra.datasource.state.DataSourceStateManager;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DbProxy resource meta data.
 */
@Getter
public final class DbProxyResourceMetaData {

    private final Map<String, DataSource> dataSources;

    private final Map<String, DatabaseType> storageTypes;

    @Getter(AccessLevel.NONE)
    private final Map<String, DataSourceMetaData> dataSourceMetaDataMap;

    public DbProxyResourceMetaData(final String databaseName, final Map<String, DataSource> dataSources) {
        this.dataSources = dataSources;
        Map<String, DataSource> enabledDataSources = DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, dataSources);
        storageTypes = createStorageTypes(enabledDataSources);
        dataSourceMetaDataMap = createDataSourceMetaDataMap(enabledDataSources, storageTypes);
    }

    private Map<String, DatabaseType> createStorageTypes(final Map<String, DataSource> enabledDataSources) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(dataSources.size(), 1);
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            DatabaseType storageType = enabledDataSources.containsKey(entry.getKey()) ? DatabaseTypeEngine.getStorageType(Collections.singletonList(entry.getValue()))
                    : DatabaseTypeEngine.getStorageType(Collections.emptyList());
            result.put(entry.getKey(), storageType);
        }
        return result;
    }

    private Map<String, DataSourceMetaData> createDataSourceMetaDataMap(final Map<String, DataSource> dataSources, final Map<String, DatabaseType> storageTypes) {
        Map<String, DataSourceMetaData> result = new LinkedHashMap<>(dataSources.size(), 1);
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            Map<String, Object> standardProps = DataSourcePropertiesCreator.create(entry.getValue()).getConnectionPropertySynonyms().getStandardProperties();
            DatabaseType storageType = storageTypes.get(entry.getKey());
            result.put(entry.getKey(), storageType.getDataSourceMetaData(standardProps.get("url").toString(), standardProps.get("username").toString()));
        }
        return result;
    }

    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (Map.Entry<String, DataSourceMetaData> entry : dataSourceMetaDataMap.entrySet()) {
            if (!isExisted(entry.getKey(), result)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        return existedDataSourceNames.stream().anyMatch(each -> dataSourceMetaDataMap.get(dataSourceName).isInSameDatabaseInstance(dataSourceMetaDataMap.get(each)));
    }

    /**
     * Get data source meta data.
     *
     * @param dataSourceName data source name
     * @return data source meta data
     */
    public DataSourceMetaData getDataSourceMetaData(final String dataSourceName) {
        return dataSourceMetaDataMap.get(dataSourceName);
    }

    /**
     * Get storage type.
     *
     * @param dataSourceName data source name
     * @return storage type
     */
    public DatabaseType getStorageType(final String dataSourceName) {
        return storageTypes.get(dataSourceName);
    }

    /**
     * Get not existed resource name.
     *
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedDataSources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !dataSources.containsKey(each)).collect(Collectors.toSet());
    }

    /**
     * Close data source.
     *
     * @param dataSource data source to be closed
     */
    public void close(final DataSource dataSource) {
        new DataSourcePoolDestroyer(dataSource).asyncDestroy();
    }
}

