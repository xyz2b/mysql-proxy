package org.xyz.dbproxy.infra.datasource.pool.metadata;

/**
 * Data source pool field meta data.
 */
public interface DataSourcePoolFieldMetaData {

    /**
     * Get username field name.
     *
     * @return username field name
     */
    String getUsernameFieldName();

    /**
     * Get password field name.
     *
     * @return password field name
     */
    String getPasswordFieldName();

    /**
     * Get JDBC URL field name.
     *
     * @return JDBC URL field name
     */
    String getJdbcUrlFieldName();

    /**
     * Get JDBC URL properties field name.
     *
     * @return JDBC URL properties field name
     */
    String getJdbcUrlPropertiesFieldName();
}