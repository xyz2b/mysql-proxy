package org.xyz.dbproxy.infra.metadata.database;

import lombok.Getter;
import org.xyz.dbproxy.infra.database.type.DatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DbProxy database.
 */
@Getter
public final class DbProxyDatabase {

    private final String name;

    private final DatabaseType protocolType;

    private final ShardingSphereResourceMetaData resourceMetaData;

    private final ShardingSphereRuleMetaData ruleMetaData;

    private final Map<String, ShardingSphereSchema> schemas;

    public DbProxyDatabase(final String name, final DatabaseType protocolType, final ShardingSphereResourceMetaData resourceMetaData,
                                  final ShardingSphereRuleMetaData ruleMetaData, final Map<String, ShardingSphereSchema> schemas) {
        this.name = name;
        this.protocolType = protocolType;
        this.resourceMetaData = resourceMetaData;
        this.ruleMetaData = ruleMetaData;
        this.schemas = new ConcurrentHashMap<>(schemas.size(), 1);
        schemas.forEach((key, value) -> this.schemas.put(key.toLowerCase(), value));
    }

    /**
     * Create database meta data.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param storageTypes storage types
     * @param databaseConfig database configuration
     * @param props configuration properties
     * @param instanceContext instance context
     * @return database meta data
     * @throws SQLException SQL exception
     */
    public static DbProxyDatabase create(final String name, final DatabaseType protocolType, final Map<String, DatabaseType> storageTypes,
                                                final DatabaseConfiguration databaseConfig, final ConfigurationProperties props, final InstanceContext instanceContext) throws SQLException {
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(name, databaseConfig, instanceContext);
        Map<String, ShardingSphereSchema> schemas = new ConcurrentHashMap<>(GenericSchemaBuilder
                .build(new GenericSchemaBuilderMaterial(protocolType, storageTypes, DataSourceStateManager.getInstance().getEnabledDataSourceMap(name, databaseConfig.getDataSources()), databaseRules,
                        props, DatabaseTypeEngine.getDefaultSchemaName(protocolType, name))));
        SystemSchemaBuilder.build(name, protocolType).forEach(schemas::putIfAbsent);
        return create(name, protocolType, databaseConfig, databaseRules, schemas);
    }

    /**
     * Create system database meta data.
     *
     * @param name system database name
     * @param protocolType protocol database type
     * @return system database meta data
     */
    public static DbProxyDatabase create(final String name, final DatabaseType protocolType) {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(new LinkedHashMap<>(), new LinkedList<>());
        return create(name, protocolType, databaseConfig, new LinkedList<>(), SystemSchemaBuilder.build(name, protocolType));
    }

    /**
     * Create database meta data.
     *
     * @param name database name
     * @param protocolType database protocol type
     * @param databaseConfig database configuration
     * @param rules rules
     * @param schemas schemas
     * @return database meta data
     */
    public static ShardingSphereDatabase create(final String name, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                final Collection<ShardingSphereRule> rules, final Map<String, ShardingSphereSchema> schemas) {
        ShardingSphereResourceMetaData resourceMetaData = createResourceMetaData(name, databaseConfig.getDataSources());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        return new ShardingSphereDatabase(name, protocolType, resourceMetaData, ruleMetaData, schemas);
    }

    private static ShardingSphereResourceMetaData createResourceMetaData(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        return new ShardingSphereResourceMetaData(databaseName, dataSourceMap);
    }

    /**
     * Get schema.
     *
     * @param schemaName schema name
     * @return schema
     */
    public ShardingSphereSchema getSchema(final String schemaName) {
        return schemas.get(schemaName.toLowerCase());
    }

    /**
     * Put schema.
     *
     * @param schemaName schema name
     * @param schema schema
     */
    public void putSchema(final String schemaName, final ShardingSphereSchema schema) {
        schemas.put(schemaName.toLowerCase(), schema);
    }

    /**
     * Remove schema.
     *
     * @param schemaName schema name
     */
    public void removeSchema(final String schemaName) {
        schemas.remove(schemaName.toLowerCase());
    }

    /**
     * Judge contains schema from database or not.
     *
     * @param schemaName schema name
     * @return contains schema from database or not
     */
    public boolean containsSchema(final String schemaName) {
        return schemas.containsKey(schemaName.toLowerCase());
    }

    /**
     * Judge whether is completed.
     *
     * @return is completed or not
     */
    public boolean isComplete() {
        return !ruleMetaData.getRules().isEmpty() && !resourceMetaData.getDataSources().isEmpty();
    }

    /**
     * Judge whether contains data source.
     *
     * @return contains data source or not
     */
    public boolean containsDataSource() {
        return !resourceMetaData.getDataSources().isEmpty();
    }

    /**
     * Reload rules.
     *
     * @param ruleClass to be reloaded rule class
     */
    public synchronized void reloadRules(final Class<? extends ShardingSphereRule> ruleClass) {
        Collection<? extends ShardingSphereRule> toBeReloadedRules = ruleMetaData.findRules(ruleClass);
        RuleConfiguration ruleConfig = toBeReloadedRules.stream().map(ShardingSphereRule::getConfiguration).findFirst().orElse(null);
        Collection<ShardingSphereRule> databaseRules = new LinkedList<>(ruleMetaData.getRules());
        toBeReloadedRules.stream().findFirst().ifPresent(optional -> {
            databaseRules.removeAll(toBeReloadedRules);
            databaseRules.add(((MutableDataNodeRule) optional).reloadRule(ruleConfig, name, resourceMetaData.getDataSources(), databaseRules));
        });
        ruleMetaData.getRules().clear();
        ruleMetaData.getRules().addAll(databaseRules);
    }
}
