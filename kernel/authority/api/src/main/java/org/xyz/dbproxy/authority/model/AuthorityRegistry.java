package org.xyz.dbproxy.authority.model;

import org.xyz.dbproxy.infra.metadata.user.Grantee;

import java.util.Optional;

/**
 * Authority registry.
 */
public interface AuthorityRegistry {

    /**
     * Find privileges.
     *
     * @param grantee grantee
     * @return found privileges
     */
    Optional<DbProxyPrivileges> findPrivileges(Grantee grantee);
}