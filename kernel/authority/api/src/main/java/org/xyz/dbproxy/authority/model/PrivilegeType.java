package org.xyz.dbproxy.authority.model;

/**
 * Privilege Type.
 */
public enum PrivilegeType {

    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    CREATE,
    ALTER,
    DROP,
    GRANT,
    INDEX,
    REFERENCES,
    LOCK_TABLES,
    CREATE_VIEW,
    SHOW_VIEW,
    EXECUTE,
    EVENT,
    TRIGGER,
    SUPER,
    SHOW_DB,
    RELOAD,
    SHUTDOWN,
    PROCESS,
    FILE,
    CREATE_TMP,
    REPL_SLAVE,
    REPL_CLIENT,
    CREATE_PROC,
    ALTER_PROC,
    CREATE_USER,
    CREATE_TABLESPACE,
    CREATE_ROLE,
    DROP_ROLE,
    TRUNCATE,
    USAGE,
    CONNECT,
    TEMPORARY,
    CREATE_DATABASE,
    INHERIT,
    CAN_LOGIN,
    CREATE_FUNCTION,
    CREATE_TABLE,
    BACKUP_DATABASE,
    CREATE_DEFAULT,
    BACKUP_LOG,
    CREATE_RULE,
    CREATE_SEQUENCE,
    CREATE_TYPE,
    CREATE_SESSION,
    ALTER_SESSION,
    CREATE_SYNONYM,
    ADMINISTER_BULK_OPERATIONS,
    ALTER_ANY_AVAILABILITY_GROUP,
    ALTER_ANY_CONNECTION,
    ALTER_ANY_CREDENTIAL,
    ALTER_ANY_DATABASE,
    ALTER_ANY_ENDPOINT,
    ALTER_ANY_EVENT_NOTIFICATION,
    ALTER_ANY_EVENT_SESSION,
    ALTER_ANY_LINKED_SERVER,
    ALTER_ANY_LOGIN,
    ALTER_ANY_SERVER_AUDIT,
    ALTER_ANY_SERVER_ROLE,
    ALTER_RESOURCES,
    ALTER_SERVER_STATE,
    ALTER_SETTINGS,
    ALTER_TRACE,
    AUTHENTICATE_SERVER,
    CONNECT_ANY_DATABASE,
    CONNECT_SQL,
    CONTROL_SERVER,
    CREATE_ANY_DATABASE,
    CREATE_AVAILABILITY_GROUP,
    CREATE_DDL_EVENT_NOTIFICATION,
    CREATE_ENDPOINT,
    CREATE_SERVER_ROLE,
    CREATE_TRACE_EVENT_NOTIFICATION,
    EXTERNAL_ACCESS_ASSEMBLY,
    IMPERSONATE_ANY_LOGIN,
    SELECT_ALL_USER_SECURABLES,
    UNSAFE_ASSEMBLY,
    VIEW_ANY_DATABASE,
    VIEW_ANY_DEFINITION,
    VIEW_SERVER_STATE
}