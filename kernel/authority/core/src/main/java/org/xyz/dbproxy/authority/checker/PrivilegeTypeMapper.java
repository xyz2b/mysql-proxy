package org.xyz.dbproxy.authority.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.authority.model.PrivilegeType;
import org.xyz.dbproxy.sql.parser.sql.common.statement.SQLStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

/**
 * Privilege type mapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeTypeMapper {

    /**
     * Get privilege type.
     *
     * @param sqlStatement SQL statement
     * @return privilege type
     */
    public static PrivilegeType getPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLPrivilegeType(sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLPrivilegeType(sqlStatement);
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }

    private static PrivilegeType getDMLPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return PrivilegeType.SELECT;
        }
        if (sqlStatement instanceof InsertStatement) {
            return PrivilegeType.INSERT;
        }
        if (sqlStatement instanceof UpdateStatement) {
            return PrivilegeType.UPDATE;
        }
        if (sqlStatement instanceof DeleteStatement) {
            return PrivilegeType.DELETE;
        }
        return null;
    }

    private static PrivilegeType getDDLPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof AlterDatabaseStatement) {
            return PrivilegeType.ALTER_ANY_DATABASE;
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return PrivilegeType.ALTER;
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return PrivilegeType.CREATE_DATABASE;
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return PrivilegeType.CREATE_TABLE;
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return PrivilegeType.CREATE_FUNCTION;
        }
        if (sqlStatement instanceof DropTableStatement || sqlStatement instanceof DropDatabaseStatement) {
            return PrivilegeType.DROP;
        }
        if (sqlStatement instanceof TruncateStatement) {
            return PrivilegeType.TRUNCATE;
        }
        return null;
    }
}
