package org.xyz.dbproxy.authority.checker;

import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.authority.model.DbProxyPrivileges;
import org.xyz.dbproxy.authority.model.PrivilegeType;
import org.xyz.dbproxy.authority.rule.AuthorityRule;
import org.xyz.dbproxy.infra.metadata.user.Grantee;
import org.xyz.dbproxy.infra.util.exception.DbProxyPreconditions;

import java.util.Collections;
import java.util.Optional;

/**
 * Authority checker.
 */
@RequiredArgsConstructor
public final class AuthorityChecker {

    private final AuthorityRule rule;

    private final Grantee grantee;

    /**
     * Check database authority.
     *
     * @param databaseName database name
     * @return authorized or not
     */
    public boolean isAuthorized(final String databaseName) {
        return null == grantee || rule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(databaseName)).orElse(false);
    }

    /**
     * Check privileges.
     *
     * @param databaseName database name
     * @param sqlStatement SQL statement
     */
    public void checkPrivileges(final String databaseName, final SQLStatement sqlStatement) {
        if (null == grantee) {
            return;
        }
        Optional<DbProxyPrivileges> privileges = rule.findPrivileges(grantee);
        DbProxyPreconditions.checkState(null == databaseName || privileges.filter(optional -> optional.hasPrivileges(databaseName)).isPresent(),
                () -> new UnknownDatabaseException(databaseName));
        PrivilegeType privilegeType = PrivilegeTypeMapper.getPrivilegeType(sqlStatement);
        DbProxyPreconditions.checkState(privileges.isPresent() && privileges.get().hasPrivileges(Collections.singleton(privilegeType)),
                () -> new UnauthorizedOperationException(null == privilegeType ? "" : privilegeType.name()));
    }
}