package org.xyz.dbproxy.infra.rule;

import org.xyz.dbproxy.infra.config.rule.RuleConfiguration;

/**
 * ShardingSphere rule.
 */
public interface DbProxyRule {

    /**
     * Get rule configuration.
     *
     * @return rule configuration
     */
    RuleConfiguration getConfiguration();

    /**
     * Get type.
     *
     * @return rule type
     */
    String getType();
}