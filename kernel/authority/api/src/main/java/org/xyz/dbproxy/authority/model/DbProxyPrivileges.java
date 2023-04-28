package org.xyz.dbproxy.authority.model;

import java.util.Collection;

/**
 * ShardingSphere privileges.
 */
public interface DbProxyPrivileges {

    /**
     * Has privileges.
     *
     * @param database schema
     * @return has privileges or not
     */
    boolean hasPrivileges(String database);

    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    boolean hasPrivileges(Collection<PrivilegeType> privileges);

    /**
     * Has privileges.
     *
     * @param accessSubject access subject
     * @param privileges privileges
     * @return has privileges or not
     */
    boolean hasPrivileges(AccessSubject accessSubject, Collection<PrivilegeType> privileges);
}
