package org.xyz.dbproxy.infra.config.database;

import org.xyz.dbproxy.infra.config.rule.RuleConfiguration;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Database configuration.
 */
public interface DatabaseConfiguration {

    /**
     * Get data sources.
     *
     * @return data sources
     */
    Map<String, DataSource> getDataSources();

    /**
     * Get rule configurations.
     *
     * @return rule configurations
     */
    Collection<RuleConfiguration> getRuleConfigurations();
}
