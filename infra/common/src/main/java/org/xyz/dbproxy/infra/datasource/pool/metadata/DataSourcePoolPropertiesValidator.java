package org.xyz.dbproxy.infra.datasource.pool.metadata;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * Data source pool properties validator.
 */
public interface DataSourcePoolPropertiesValidator {

    /**
     * Check properties.
     *
     * @param dataSourceProps Data source properties
     */
    void validateProperties(DataSourceProperties dataSourceProps);
}

