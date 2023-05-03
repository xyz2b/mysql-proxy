package org.xyz.dbproxy.authority.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.infra.config.algorithm.AlgorithmConfiguration;
import org.xyz.dbproxy.infra.config.rule.scope.GlobalRuleConfiguration;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authority rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class AuthorityRuleConfiguration implements GlobalRuleConfiguration {

    // DbProxy上面配置的用户列表
    private final Collection<DbProxyUser> users;

    // 加密算法
    private final AlgorithmConfiguration authorityProvider;

    // 默认的认证器（mysql_native_password）
    private final String defaultAuthenticator;

    private final Map<String, AlgorithmConfiguration> authenticators = new LinkedHashMap<>();
}
