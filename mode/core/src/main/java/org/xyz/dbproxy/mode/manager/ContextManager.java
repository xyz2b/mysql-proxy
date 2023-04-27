package org.xyz.dbproxy.mode.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {

    private volatile MetaDataContexts metaDataContexts;

    private final InstanceContext instanceContext;

    private final ExecutorEngine executorEngine;

    private final ClusterStateContext clusterStateContext = new ClusterStateContext();

    public ContextManager(final MetaDataContexts metaDataContexts, final InstanceContext instanceContext) {
        this.metaDataContexts = metaDataContexts;
        this.instanceContext = instanceContext;
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }

    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    public synchronized void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts = metaDataContexts;
    }

    /**
     * Get data source map.
     *
     * @param databaseName database name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String databaseName) {
        return metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources();
    }

    /**
     * Add database.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabase(final String databaseName) {
        if (metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(Collections.emptyMap(), metaDataContexts.getMetaData().getProps());
        metaDataContexts.getMetaData().addDatabase(databaseName, protocolType);
    }

    /**
     * Drop database.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabase(final String databaseName) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName)) {
            return;
        }
        String actualDatabaseName = metaDataContexts.getMetaData().getActualDatabaseName(databaseName);
        metaDataContexts.getMetaData().dropDatabase(actualDatabaseName);
    }

    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addSchema(final String databaseName, final String schemaName) {
        if (metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getMetaData().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchema());
    }

    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropSchema(final String databaseName, final String schemaName) {
        if (!metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getMetaData().getDatabase(databaseName).removeSchema(schemaName);
    }

    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeDeletedTableName to be deleted table name
     * @param toBeDeletedViewName to be deleted view name
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final String toBeDeletedTableName, final String toBeDeletedViewName) {
        Optional.ofNullable(toBeDeletedTableName).ifPresent(optional -> dropTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeDeletedViewName).ifPresent(optional -> dropView(databaseName, schemaName, optional));
    }

    /**
     * Alter schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param toBeChangedTable to be changed table
     * @param toBeChangedView to be changed view
     */
    public synchronized void alterSchema(final String databaseName, final String schemaName, final ShardingSphereTable toBeChangedTable, final ShardingSphereView toBeChangedView) {
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        Optional.ofNullable(toBeChangedTable).ifPresent(optional -> alterTable(databaseName, schemaName, optional));
        Optional.ofNullable(toBeChangedView).ifPresent(optional -> alterView(databaseName, schemaName, optional));
    }

    private synchronized void dropTable(final String databaseName, final String schemaName, final String toBeDeletedTableName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeTable(toBeDeletedTableName);
        metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedTableName));
    }

    private synchronized void dropView(final String databaseName, final String schemaName, final String toBeDeletedViewName) {
        metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).removeView(toBeDeletedViewName);
        metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules().stream().filter(each -> each instanceof MutableDataNodeRule).findFirst()
                .ifPresent(optional -> ((MutableDataNodeRule) optional).remove(schemaName, toBeDeletedViewName));
    }

    private synchronized void alterTable(final String databaseName, final String schemaName, final ShardingSphereTable beBoChangedTable) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (!containsMutableDataNodeRule(database, beBoChangedTable.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putTable(beBoChangedTable.getName(), beBoChangedTable);
    }

    private synchronized void alterView(final String databaseName, final String schemaName, final ShardingSphereView beBoChangedView) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        if (!containsMutableDataNodeRule(database, beBoChangedView.getName())) {
            database.reloadRules(MutableDataNodeRule.class);
        }
        database.getSchema(schemaName).putView(beBoChangedView.getName(), beBoChangedView);
    }

    private boolean containsMutableDataNodeRule(final ShardingSphereDatabase database, final String tableName) {
        return database.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }

    /**
     * Alter rule configuration.
     *
     * @param databaseName database name
     * @param ruleConfigs rule configurations
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, null, ruleConfigs);
            alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadMetaDataContexts;
            metaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().putAll(newShardingSphereSchemas(metaDataContexts.getMetaData().getDatabase(databaseName)));
        } catch (final SQLException ex) {
            log.error("Alter database: {} rule configurations failed", databaseName, ex);
        }
    }

    /**
     * Alter schema meta data.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public synchronized void alterSchemaMetaData(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Map<String, ShardingSphereSchema> toBeAlterSchemas = SchemaManager.getToBeDeletedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        Map<String, ShardingSphereSchema> toBeAddedSchemas = SchemaManager.getToBeAddedTablesBySchemas(reloadDatabase.getSchemas(), currentDatabase.getSchemas());
        toBeAddedSchemas.forEach((key, value) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().persist(databaseName, key, value));
        toBeAlterSchemas.forEach((key, value) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().delete(databaseName, key, value));
    }

    /**
     * Alter data source configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap altered data source properties map
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterDataSourceConfiguration(final String databaseName, final Map<String, DataSourceProperties> dataSourcePropsMap) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource =
                    new ResourceSwitchManager().createByAlterDataSourceProps(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metaDataContexts.getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), switchingResource));
            // TODO Remove this logic when issue #22887 are finished.
            MetaDataContexts reloadMetaDataContexts = createMetaDataContexts(databaseName, false, switchingResource, null);
            reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> reloadMetaDataContexts.getPersistService().getDatabaseMetaDataService()
                    .persist(reloadMetaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, schema));
            Optional.ofNullable(reloadMetaDataContexts.getShardingSphereData().getDatabaseData().get(databaseName))
                    .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> reloadMetaDataContexts.getPersistService().getShardingSphereDataPersistService()
                            .persist(databaseName, schemaName, schemaData, metaDataContexts.getMetaData().getDatabases())));
            alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadMetaDataContexts;
            metaDataContexts.getMetaData().getDatabases().putAll(newShardingSphereDatabase(metaDataContexts.getMetaData().getDatabase(databaseName)));
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Alter database: {} data source configuration failed", databaseName, ex);
        }
    }

    /**
     * Renew ShardingSphere databases.
     *
     * @param database database
     * @param resource resource
     * @return ShardingSphere databases
     */
    public synchronized Map<String, ShardingSphereDatabase> renewDatabase(final ShardingSphereDatabase database, final SwitchingResource resource) {
        Map<String, DataSource> newDataSource =
                database.getResourceMetaData().getDataSources().entrySet().stream().filter(entry -> !resource.getStaleDataSources().containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return Collections.singletonMap(database.getName().toLowerCase(),
                new ShardingSphereDatabase(database.getName(), database.getProtocolType(), new ShardingSphereResourceMetaData(database.getName(), newDataSource),
                        database.getRuleMetaData(), database.getSchemas()));
    }

    /**
     * Alter data source and rule configuration.
     *
     * @param databaseName database name
     * @param dataSourcePropsMap data source props map
     * @param ruleConfigs rule configurations
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterDataSourceAndRuleConfiguration(final String databaseName,
                                                                 final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<RuleConfiguration> ruleConfigs) {
        try {
            Collection<ResourceHeldRule> staleResourceHeldRules = getStaleResourceHeldRules(databaseName);
            staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
            SwitchingResource switchingResource = new ResourceSwitchManager().create(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), dataSourcePropsMap);
            metaDataContexts = createMetaDataContexts(databaseName, true, switchingResource, ruleConfigs);
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Alter database: {} data source and rule configuration failed", databaseName, ex);
        }
    }

    @SuppressWarnings("rawtypes")
    private Collection<ResourceHeldRule> getStaleResourceHeldRules(final String databaseName) {
        Collection<ResourceHeldRule> result = new LinkedList<>();
        result.addAll(metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().findRules(ResourceHeldRule.class));
        result.addAll(metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class));
        return result;
    }

    /**
     * Create meta data contexts.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return MetaDataContexts meta data contexts
     * @throws SQLException SQL exception
     */
    public synchronized MetaDataContexts createMetaDataContexts(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                                final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, internalLoadMetaData, switchingResource, ruleConfigs);
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        ShardingSphereRuleMetaData changedGlobalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), changedDatabases, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetaData, props));
    }

    private MetaDataContexts createMetaDataContexts(final String databaseName, final SwitchingResource switchingResource) throws SQLException {
        MetaDataPersistService metaDataPersistService = metaDataContexts.getPersistService();
        Map<String, ShardingSphereDatabase> changedDatabases = createChangedDatabases(databaseName, false,
                switchingResource, metaDataPersistService.getDatabaseRulePersistService().load(databaseName));
        ConfigurationProperties props = new ConfigurationProperties(metaDataPersistService.getPropsService().load());
        ShardingSphereRuleMetaData changedGlobalMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(metaDataPersistService.getGlobalRuleService().load(), changedDatabases, props));
        return newMetaDataContexts(new ShardingSphereMetaData(changedDatabases, changedGlobalMetaData, props));
    }

    /**
     * Create changed databases.
     *
     * @param databaseName database name
     * @param internalLoadMetaData internal load meta data
     * @param switchingResource switching resource
     * @param ruleConfigs rule configs
     * @return ShardingSphere databases
     * @throws SQLException SQL exception
     */
    public synchronized Map<String, ShardingSphereDatabase> createChangedDatabases(final String databaseName, final boolean internalLoadMetaData, final SwitchingResource switchingResource,
                                                                                   final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        if (null != switchingResource && !switchingResource.getNewDataSources().isEmpty()) {
            metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources().putAll(switchingResource.getNewDataSources());
        }
        Collection<RuleConfiguration> toBeCreatedRuleConfigs = null == ruleConfigs
                ? metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getConfigurations()
                : ruleConfigs;
        DatabaseConfiguration toBeCreatedDatabaseConfig =
                new DataSourceProvidedDatabaseConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources(), toBeCreatedRuleConfigs);
        ShardingSphereDatabase changedDatabase = MetaDataFactory.create(metaDataContexts.getMetaData().getActualDatabaseName(databaseName),
                internalLoadMetaData, metaDataContexts.getPersistService(), toBeCreatedDatabaseConfig, metaDataContexts.getMetaData().getProps(), instanceContext);
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(metaDataContexts.getMetaData().getDatabases());
        changedDatabase.getSchemas().putAll(newShardingSphereSchemas(changedDatabase));
        result.put(databaseName.toLowerCase(), changedDatabase);
        return result;
    }

    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return new MetaDataContexts(metaDataContexts.getPersistService(), metaData);
    }

    private Map<String, ShardingSphereSchema> newShardingSphereSchemas(final ShardingSphereDatabase database) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(database.getSchemas().size(), 1);
        database.getSchemas().forEach((key, value) -> result.put(key, new ShardingSphereSchema(value.getTables(),
                metaDataContexts.getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), key))));
        return result;
    }

    /**
     * Create new ShardingSphere database.
     *
     * @param originalDatabase original database
     * @return ShardingSphere databases
     */
    public synchronized Map<String, ShardingSphereDatabase> newShardingSphereDatabase(final ShardingSphereDatabase originalDatabase) {
        return Collections.singletonMap(originalDatabase.getName().toLowerCase(), new ShardingSphereDatabase(originalDatabase.getName(),
                originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(),
                metaDataContexts.getPersistService().getDatabaseMetaDataService().loadSchemas(originalDatabase.getName())));
    }

    /**
     * Alter global rule configuration.
     *
     * @param ruleConfigs global rule configuration
     */
    @SuppressWarnings("rawtypes")
    public synchronized void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> ruleConfigs) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        Collection<ResourceHeldRule> staleResourceHeldRules = metaDataContexts.getMetaData().getGlobalRuleMetaData().findRules(ResourceHeldRule.class);
        staleResourceHeldRules.forEach(ResourceHeldRule::closeStaleResource);
        ShardingSphereRuleMetaData toBeChangedGlobalRuleMetaData = new ShardingSphereRuleMetaData(
                GlobalRulesBuilder.buildRules(ruleConfigs, metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getProps()));
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), toBeChangedGlobalRuleMetaData, metaDataContexts.getMetaData().getProps());
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData);
    }

    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metaDataContexts = newMetaDataContexts(toBeChangedMetaData);
    }

    /**
     * Reload database meta data from governance center.
     *
     * @param databaseName to be reloaded database name
     */
    public synchronized void reloadDatabaseMetaData(final String databaseName) {
        try {
            ShardingSphereResourceMetaData currentResourceMetaData = metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData();
            Map<String, DataSourceProperties> dataSourceProps = metaDataContexts.getPersistService().getDataSourceService().load(databaseName);
            SwitchingResource switchingResource = new ResourceSwitchManager().createByAlterDataSourceProps(currentResourceMetaData, dataSourceProps);
            metaDataContexts.getMetaData().getDatabases().putAll(renewDatabase(metaDataContexts.getMetaData().getDatabase(databaseName), switchingResource));
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(databaseName, switchingResource);
            deletedSchemaNames(databaseName, reloadedMetaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getDatabase(databaseName));
            metaDataContexts = reloadedMetaDataContexts;
            metaDataContexts.getMetaData().getDatabases().values().forEach(
                    each -> each.getSchemas().forEach((schemaName, schema) -> metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(each.getName(), schemaName, schema)));
            switchingResource.closeStaleDataSources();
        } catch (final SQLException ex) {
            log.error("Reload database meta data: {} failed", databaseName, ex);
        }
    }

    /**
     * Delete schema names.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public synchronized void deletedSchemaNames(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        SchemaManager.getToBeDeletedSchemaNames(reloadDatabase.getSchemas(), currentDatabase.getSchemas()).keySet()
                .forEach(each -> metaDataContexts.getPersistService().getDatabaseMetaDataService().dropSchema(databaseName, each));
    }

    /**
     * Reload schema.
     *
     * @param databaseName database name
     * @param schemaName to be reloaded schema name
     * @param dataSourceName data source name
     */
    public synchronized void reloadSchema(final String databaseName, final String schemaName, final String dataSourceName) {
        try {
            ShardingSphereSchema reloadedSchema = loadSchema(databaseName, schemaName, dataSourceName);
            if (reloadedSchema.getTables().isEmpty()) {
                metaDataContexts.getMetaData().getDatabase(databaseName).removeSchema(schemaName);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().dropSchema(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName);
            } else {
                metaDataContexts.getMetaData().getDatabase(databaseName).putSchema(schemaName, reloadedSchema);
                metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), schemaName, reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload meta data of database: {} schema: {} with data source: {} failed", databaseName, schemaName, dataSourceName, ex);
        }
    }

    private ShardingSphereSchema loadSchema(final String databaseName, final String schemaName, final String dataSourceName) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        database.reloadRules(MutableDataNodeRule.class);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(), database.getResourceMetaData().getStorageTypes(),
                Collections.singletonMap(dataSourceName, database.getResourceMetaData().getDataSources().get(dataSourceName)),
                database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema result = GenericSchemaBuilder.build(material).get(schemaName);
        result.getViews().putAll(metaDataContexts.getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), schemaName));
        return result;
    }

    /**
     * Reload table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     */
    public synchronized void reloadTable(final String databaseName, final String schemaName, final String tableName) {
        Map<String, DataSource> dataSourceMap = metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources();
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} failed", tableName, databaseName, schemaName, ex);
        }
    }

    /**
     * Reload table from single data source.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName to be reloaded table name
     */
    public synchronized void reloadTable(final String databaseName, final String schemaName, final String dataSourceName, final String tableName) {
        Map<String, DataSource> dataSourceMap = Collections.singletonMap(
                dataSourceName, metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources().get(dataSourceName));
        try {
            reloadTable(databaseName, schemaName, tableName, dataSourceMap);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} with data source: {} failed", tableName, databaseName, schemaName, dataSourceName, ex);
        }
    }

    private synchronized void reloadTable(final String databaseName, final String schemaName, final String tableName, final Map<String, DataSource> dataSourceMap) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                database.getResourceMetaData().getStorageTypes(), dataSourceMap, database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singletonList(tableName), material).getOrDefault(schemaName, new ShardingSphereSchema());
        if (schema.containsTable(tableName)) {
            alterTable(databaseName, schemaName, schema.getTable(tableName));
        } else {
            dropTable(databaseName, schemaName, tableName);
        }
        metaDataContexts.getPersistService().getDatabaseMetaDataService().compareAndPersist(database.getName(), schemaName, database.getSchema(schemaName));
    }

    /**
     * Add ShardingSphere database data.
     *
     * @param databaseName database name
     */
    public synchronized void addShardingSphereDatabaseData(final String databaseName) {
        if (metaDataContexts.getShardingSphereData().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().putDatabase(databaseName, new ShardingSphereDatabaseData());
    }

    /**
     * Drop ShardingSphere data database.
     *
     * @param databaseName database name
     */
    public synchronized void dropShardingSphereDatabaseData(final String databaseName) {
        if (!metaDataContexts.getShardingSphereData().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().dropDatabase(databaseName);
    }

    /**
     * Add ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addShardingSphereSchemaData(final String databaseName, final String schemaName) {
        if (metaDataContexts.getShardingSphereData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchemaData());
    }

    /**
     * Drop ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropShardingSphereSchemaData(final String databaseName, final String schemaName) {
        ShardingSphereDatabaseData databaseData = metaDataContexts.getShardingSphereData().getDatabase(databaseName);
        if (null == databaseData || !databaseData.containsSchema(schemaName)) {
            return;
        }
        databaseData.removeSchema(schemaName);
    }

    /**
     * Add ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void addShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getShardingSphereData().containsDatabase(databaseName) || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        if (metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).putTable(tableName, new ShardingSphereTableData(tableName));
    }

    /**
     * Drop ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getShardingSphereData().containsDatabase(databaseName) || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).removeTable(tableName);
    }

    /**
     * Alter ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param yamlRowData yaml row data
     */
    public synchronized void alterShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final YamlShardingSphereRowData yamlRowData) {
        if (!metaDataContexts.getShardingSphereData().containsDatabase(databaseName) || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
        List<ShardingSphereColumn> columns = new ArrayList<>(metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getColumns().values());
        tableData.getRows().add(new YamlShardingSphereRowDataSwapper(columns).swapToObject(yamlRowData));
    }

    /**
     * Delete ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey row uniqueKey
     */
    public synchronized void deleteShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        if (!metaDataContexts.getShardingSphereData().containsDatabase(databaseName) || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getShardingSphereData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getRows().removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }

    /**
     * Update cluster state.
     *
     * @param status status
     */
    public void updateClusterState(final String status) {
        try {
            clusterStateContext.switchState(ClusterState.valueOf(status));
        } catch (final IllegalArgumentException ignore) {
        }
    }

    @Override
    public void close() {
        executorEngine.close();
        metaDataContexts.close();
    }
}
