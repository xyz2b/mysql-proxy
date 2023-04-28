package org.xyz.dbproxy.infra.datasource.props;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.infra.datasource.config.DataSourceConfiguration;
import org.xyz.dbproxy.infra.datasource.pool.creator.DataSourceReflection;
import org.xyz.dbproxy.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data source properties creator.
 * 创建数据源的一些配置信息
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertiesCreator {

    /**
     * Create data source properties.
     *
     * @param dataSourcePoolClassName data source pool class name
     * @param dataSourceConfig data source configuration
     * @return created data source properties
     */
    public static DataSourceProperties create(final String dataSourcePoolClassName, final DataSourceConfiguration dataSourceConfig) {
        return new DataSourceProperties(dataSourcePoolClassName, createProperties(dataSourceConfig));
    }

    /**
     * Create data source properties.
     *
     * @param dataSource data source
     * @return created data source properties
     */
    public static DataSourceProperties create(final DataSource dataSource) {
        return new DataSourceProperties(dataSource.getClass().getName(), createProperties(dataSource));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> createProperties(final DataSourceConfiguration dataSourceConfig) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", dataSourceConfig.getConnection().getUrl());
        result.put("username", dataSourceConfig.getConnection().getUsername());
        result.put("password", dataSourceConfig.getConnection().getPassword());
        result.put("connectionTimeoutMilliseconds", dataSourceConfig.getPool().getConnectionTimeoutMilliseconds());
        result.put("idleTimeoutMilliseconds", dataSourceConfig.getPool().getIdleTimeoutMilliseconds());
        result.put("maxLifetimeMilliseconds", dataSourceConfig.getPool().getMaxLifetimeMilliseconds());
        result.put("maxPoolSize", dataSourceConfig.getPool().getMaxPoolSize());
        result.put("minPoolSize", dataSourceConfig.getPool().getMinPoolSize());
        result.put("readOnly", dataSourceConfig.getPool().getReadOnly());
        if (null != dataSourceConfig.getPool().getCustomProperties()) {
            result.putAll((Map) dataSourceConfig.getPool().getCustomProperties());
        }
        return result;
    }

    private static Map<String, Object> createProperties(final DataSource dataSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSource.getClass().getName());
        for (Map.Entry<String, Object> entry : new DataSourceReflection(dataSource).convertToProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (!poolMetaData.isPresent() || isValidProperty(propertyName, propertyValue, poolMetaData.get()) && !poolMetaData.get().getTransientFieldNames().contains(propertyName)) {
                result.put(propertyName, propertyValue);
            }
        }
        return result;
    }

    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getInvalidProperties().get(key));
    }
}
