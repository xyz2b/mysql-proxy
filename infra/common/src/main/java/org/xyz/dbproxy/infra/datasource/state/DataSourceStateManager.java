package org.xyz.dbproxy.infra.datasource.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.infra.config.database.DatabaseConfiguration;
import org.xyz.dbproxy.infra.datasource.state.exception.UnavailableDataSourceException;
import org.xyz.dbproxy.infra.util.exception.DbProxyPreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source state manager.
 * 数据源管理器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataSourceStateManager {

    private static final DataSourceStateManager INSTANCE = new DataSourceStateManager();

    private final Map<String, DataSourceState> dataSourceStates = new ConcurrentHashMap<>();

    private volatile boolean forceStart;

    private volatile boolean initialized;

    /**
     * Get data source state manager.
     *
     * @return data source state manager
     */
    public static DataSourceStateManager getInstance() {
        return INSTANCE;
    }

    /**
     * Set data source states when bootstrap.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @param storageDataSourceStates storage node data source state
     * @param forceStart whether to force start
     */
    public void initStates(final String databaseName, final Map<String, DataSource> dataSources, final Map<String, DataSourceState> storageDataSourceStates, final boolean forceStart) {
        this.forceStart = forceStart;
        dataSources.forEach((key, value) -> initState(databaseName, storageDataSourceStates, key, value));
        initialized = true;
    }

    // 将enable状态的datasource和disable状态的datasource分别放入dataSourceStates中
    private void initState(final String databaseName, final Map<String, DataSourceState> storageDataSourceStates, final String actualDataSourceName, final DataSource dataSource) {
        DataSourceState storageState = storageDataSourceStates.get(getCacheKey(databaseName, actualDataSourceName));
        if (DataSourceState.DISABLED == storageState) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), storageState);
        } else {
            checkState(databaseName, actualDataSourceName, dataSource);
        }
    }

    // enable的datasource需要检查下是否能够正常连接
    private void checkState(final String databaseName, final String actualDataSourceName, final DataSource dataSource) {
        // 尝试获取一下datasource的连接，获取成功才认为这个enable的datasource可用，才会加入到dataSourceStates中
        try (Connection ignored = dataSource.getConnection()) {
            dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), DataSourceState.ENABLED);
        } catch (final SQLException ex) {
            DbProxyPreconditions.checkState(forceStart, () -> new UnavailableDataSourceException(ex, actualDataSourceName));
            log.error("Data source unavailable, ignored with the -f parameter.", ex);
        }
    }

    /**
     * Get enabled data sources.
     *
     * @param databaseName database name
     * @param databaseConfig database config
     * @return enabled data sources
     */
    public Collection<DataSource> getEnabledDataSources(final String databaseName, final DatabaseConfiguration databaseConfig) {
        return databaseConfig.getDataSources().isEmpty() ? Collections.emptyList() : getEnabledDataSourceMap(databaseName, databaseConfig.getDataSources()).values();
    }

    /**
     * Get enabled data source map.
     *
     * @param databaseName database name
     * @param dataSources data sources
     * @return enabled data source map
     */
    public Map<String, DataSource> getEnabledDataSourceMap(final String databaseName, final Map<String, DataSource> dataSources) {
        if (dataSources.isEmpty() || !initialized) {
            return dataSources;
        }
        Map<String, DataSource> result = filterDisabledDataSources(databaseName, dataSources);
        // 如果需要强制启动，会去检查datasource的连接是否正常
        checkForceConnection(result);
        return result;
    }

    private Map<String, DataSource> filterDisabledDataSources(final String databaseName, final Map<String, DataSource> dataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1);
        dataSources.forEach((key, value) -> {
            DataSourceState dataSourceState = dataSourceStates.get(getCacheKey(databaseName, key));
            if (DataSourceState.DISABLED != dataSourceState) {
                result.put(key, value);
            }
        });
        return result;
    }

    private void checkForceConnection(final Map<String, DataSource> dataSources) {
        if (forceStart) {
            dataSources.entrySet().removeIf(entry -> {
                try (Connection ignored = entry.getValue().getConnection()) {
                    return false;
                } catch (final SQLException ex) {
                    log.error("Data source state unavailable, ignored with the -f parameter.", ex);
                    return true;
                }
            });
        }
    }

    /**
     * Update data source state.
     *
     * @param databaseName database name
     * @param actualDataSourceName actual data source name
     * @param dataSourceState data source state
     */
    public void updateState(final String databaseName, final String actualDataSourceName, final DataSourceState dataSourceState) {
        dataSourceStates.put(getCacheKey(databaseName, actualDataSourceName), dataSourceState);
    }

    private String getCacheKey(final String databaseName, final String dataSourceName) {
        return databaseName + "." + dataSourceName;
    }
}
