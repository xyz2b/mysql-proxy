package org.xyz.dbproxy.infra.datasource.pool.metadata.type;

import org.xyz.dbproxy.infra.datasource.pool.metadata.DataSourcePoolFieldMetaData;

/**
 * Default data source pool field meta data.
 */
public final class DefaultDataSourcePoolFieldMetaData implements DataSourcePoolFieldMetaData {

    @Override
    public String getUsernameFieldName() {
        return "username";
    }

    @Override
    public String getPasswordFieldName() {
        return "password";
    }

    @Override
    public String getJdbcUrlFieldName() {
        return "url";
    }

    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return null;
    }
}

