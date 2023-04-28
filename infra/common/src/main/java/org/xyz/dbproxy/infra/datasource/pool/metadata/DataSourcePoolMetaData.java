package org.xyz.dbproxy.infra.datasource.pool.metadata;

import org.xyz.dbproxy.infra.util.spi.annotation.SingletonSPI;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.Map;

/**
 * Data source pool meta data.
 */
@SingletonSPI
public interface DataSourcePoolMetaData extends TypedSPI {

    /**
     * Get default properties.
     *
     * @return default properties
     */
    Map<String, Object> getDefaultProperties();

    /**
     * Get invalid properties.
     *
     * @return invalid properties
     */
    Map<String, Object> getInvalidProperties();

    /**
     * Get property synonyms.
     *
     * @return property synonyms
     */
    Map<String, String> getPropertySynonyms();

    /**
     * Get transient field names.
     *
     * @return transient field names
     */
    Collection<String> getTransientFieldNames();

    /**
     * Get data source pool field meta data.
     *
     * @return data source pool field meta data
     */
    DataSourcePoolFieldMetaData getFieldMetaData();

    /**
     * Get data source pool properties validator.
     *
     * @return data source pool properties validator
     */
    DataSourcePoolPropertiesValidator getDataSourcePoolPropertiesValidator();

}