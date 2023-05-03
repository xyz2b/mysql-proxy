package org.xyz.dbproxy.authority.rule;

import lombok.Getter;
import org.xyz.dbproxy.authority.config.AuthorityRuleConfiguration;
import org.xyz.dbproxy.authority.model.AuthorityRegistry;
import org.xyz.dbproxy.authority.model.DbProxyPrivileges;
import org.xyz.dbproxy.infra.metadata.database.DbProxyDatabase;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;
import org.xyz.dbproxy.infra.metadata.user.Grantee;
import org.xyz.dbproxy.infra.rule.identifier.scope.GlobalRule;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Map;
import java.util.Optional;

/**
 * Authority rule.
 */
public final class AuthorityRule implements GlobalRule {

    // 用户密码配置
    @Getter
    private final AuthorityRuleConfiguration configuration;

    // 权限配置（根据username@hostname找到其所具有的库表的相应权限，select insert等）
    private final AuthorityRegistry authorityRegistry;

    public AuthorityRule(final AuthorityRuleConfiguration ruleConfig, final Map<String, DbProxyDatabase> databases) {
        configuration = ruleConfig;
        AuthorityProvider provider = TypedSPILoader.getService(AuthorityProvider.class, ruleConfig.getAuthorityProvider().getType(), ruleConfig.getAuthorityProvider().getProps());
        authorityRegistry = provider.buildAuthorityRegistry(databases, ruleConfig.getUsers());
    }

    /**
     * Get authenticator type.
     *
     * @param user user
     * @return authenticator type
     */
    public String getAuthenticatorType(final DbProxyUser user) {
        return configuration.getAuthenticators().containsKey(user.getAuthenticationMethodName())
                ? configuration.getAuthenticators().get(user.getAuthenticationMethodName()).getType()
                : Optional.ofNullable(configuration.getDefaultAuthenticator()).orElse("");
    }

    /**
     * Find user.
     *
     * @param grantee grantee user
     * @return user
     */
    public Optional<DbProxyUser> findUser(final Grantee grantee) {
        return configuration.getUsers().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }

    /**
     * Find privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    public Optional<DbProxyPrivileges> findPrivileges(final Grantee grantee) {
        return authorityRegistry.findPrivileges(grantee);
    }

    @Override
    public String getType() {
        return AuthorityRule.class.getSimpleName();
    }
}
