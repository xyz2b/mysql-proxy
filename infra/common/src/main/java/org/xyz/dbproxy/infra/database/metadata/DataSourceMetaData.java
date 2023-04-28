package org.xyz.dbproxy.infra.database.metadata;

import java.util.Properties;

/**
 * Data source meta data.
 * 后端数据源相关信息
 */
public interface DataSourceMetaData {

    /**
     * Get host name.
     *
     * @return host name
     */
    String getHostname();

    /**
     * Get port.
     *
     * @return port
     */
    int getPort();

    /**
     * Get catalog.
     *
     * @return catalog
     */
    String getCatalog();

    /**
     * Get schema.
     *
     * @return schema
     */
    String getSchema();

    /**
     * Get query properties.
     *
     * @return query properties
     */
    Properties getQueryProperties();

    /**
     * Get default query properties.
     *
     * @return default query properties
     */
    Properties getDefaultQueryProperties();

    /**
     * Judge whether two of data sources are in the same database instance.
     *
     * @param dataSourceMetaData data source meta data
     * @return data sources are in the same database instance or not
     */
    default boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return getHostname().equals(dataSourceMetaData.getHostname()) && getPort() == dataSourceMetaData.getPort();
    }
}
