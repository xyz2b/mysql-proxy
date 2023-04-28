package org.xyz.dbproxy.infra.datasource.pool.metadata;

import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.infra.util.reflection.ReflectionUtils;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * Data source pool meta data reflection.
 */
@RequiredArgsConstructor
public final class DataSourcePoolMetaDataReflection {

    private final DataSource targetDataSource;

    private final DataSourcePoolFieldMetaData dataSourcePoolFieldMetaData;

    /**
     * Get JDBC URL.
     *
     * @return JDBC URL
     */
    public Optional<String> getJdbcUrl() {
        return ReflectionUtils.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getJdbcUrlFieldName());
    }

    /**
     * Get JDBC connection properties.
     *
     * @return JDBC connection properties
     */
    public Optional<Properties> getJdbcConnectionProperties() {
        return ReflectionUtils.getFieldValue(targetDataSource, dataSourcePoolFieldMetaData.getJdbcUrlPropertiesFieldName());
    }
}