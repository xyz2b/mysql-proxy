package org.xyz.dbproxy.infra.datasource.props;

import lombok.Getter;
import org.xyz.dbproxy.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;

import java.util.*;

/**
 * Data source properties.
 */
@Getter
public final class DataSourceProperties {

    private final String dataSourceClassName;

    private final ConnectionPropertySynonyms connectionPropertySynonyms;

    private final PoolPropertySynonyms poolPropertySynonyms;

    private final CustomDataSourceProperties customDataSourceProperties;

    public DataSourceProperties(final String dataSourceClassName, final Map<String, Object> props) {
        this.dataSourceClassName = dataSourceClassName;
        Optional<DataSourcePoolMetaData> poolMetaData = TypedSPILoader.findService(DataSourcePoolMetaData.class, dataSourceClassName);
        Map<String, String> propertySynonyms = poolMetaData.isPresent() ? poolMetaData.get().getPropertySynonyms() : Collections.emptyMap();
        connectionPropertySynonyms = new ConnectionPropertySynonyms(props, propertySynonyms);
        poolPropertySynonyms = new PoolPropertySynonyms(props, propertySynonyms);
        customDataSourceProperties = new CustomDataSourceProperties(
                props, getStandardPropertyKeys(), poolMetaData.isPresent() ? poolMetaData.get().getTransientFieldNames() : Collections.emptyList(), propertySynonyms);
    }

    private Collection<String> getStandardPropertyKeys() {
        Collection<String> result = new LinkedList<>(connectionPropertySynonyms.getStandardPropertyKeys());
        result.addAll(poolPropertySynonyms.getStandardPropertyKeys());
        return result;
    }

    /**
     * Get all standard properties.
     *
     * @return all standard properties
     */
    public Map<String, Object> getAllStandardProperties() {
        Map<String, Object> result = new LinkedHashMap<>(
                connectionPropertySynonyms.getStandardProperties().size() + poolPropertySynonyms.getStandardProperties().size() + customDataSourceProperties.getProperties().size(), 1);
        result.putAll(connectionPropertySynonyms.getStandardProperties());
        result.putAll(poolPropertySynonyms.getStandardProperties());
        result.putAll(customDataSourceProperties.getProperties());
        return result;
    }

    /**
     * Get all local properties.
     *
     * @return all local properties
     */
    public Map<String, Object> getAllLocalProperties() {
        Map<String, Object> result = new LinkedHashMap<>(
                connectionPropertySynonyms.getLocalProperties().size() + poolPropertySynonyms.getLocalProperties().size() + customDataSourceProperties.getProperties().size(), 1);
        result.putAll(connectionPropertySynonyms.getLocalProperties());
        result.putAll(poolPropertySynonyms.getLocalProperties());
        result.putAll(customDataSourceProperties.getProperties());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || null != obj && getClass() == obj.getClass() && equalsByProperties((DataSourceProperties) obj);
    }

    private boolean equalsByProperties(final DataSourceProperties dataSourceProps) {
        if (!dataSourceClassName.equals(dataSourceProps.dataSourceClassName)) {
            return false;
        }
        for (Map.Entry<String, Object> entry : getAllLocalProperties().entrySet()) {
            if (!dataSourceProps.getAllLocalProperties().containsKey(entry.getKey())) {
                continue;
            }
            if (entry.getValue() instanceof Map) {
                return entry.getValue().equals(dataSourceProps.getAllLocalProperties().get(entry.getKey()));
            }
            if (!String.valueOf(entry.getValue()).equals(String.valueOf(dataSourceProps.getAllLocalProperties().get(entry.getKey())))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : getAllLocalProperties().entrySet()) {
            stringBuilder.append(entry.getKey()).append(entry.getValue());
        }
        return com.google.common.base.Objects.hashCode(dataSourceClassName, stringBuilder.toString());
    }
}
