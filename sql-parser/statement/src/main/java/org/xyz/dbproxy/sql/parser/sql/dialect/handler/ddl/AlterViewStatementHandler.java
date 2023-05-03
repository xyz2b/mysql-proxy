/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xyz.dbproxy.sql.parser.sql.dialect.handler.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.xyz.dbproxy.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.xyz.dbproxy.sql.parser.sql.common.statement.dml.SelectStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterViewStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterViewStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterViewStatement;

import java.util.Optional;

/**
 * AlterViewStatement handler for different dialect SQLStatements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterViewStatementHandler implements SQLStatementHandler {
    
    /**
     * Get select statement.
     *
     * @param alterViewStatement AlterViewStatement
     * @return select statement
     */
    public static Optional<SelectStatement> getSelectStatement(final AlterViewStatement alterViewStatement) {
        if (alterViewStatement instanceof MySQLStatement) {
            return Optional.of(((MySQLAlterViewStatement) alterViewStatement).getSelect());
        }
        if (alterViewStatement instanceof SQLServerStatement) {
            return Optional.of(((SQLServerAlterViewStatement) alterViewStatement).getSelect());
        }
        return Optional.empty();
    }
    
    /**
     * Get view definition.
     *
     * @param alterViewStatement AlterViewStatement
     * @return view definition
     */
    public static Optional<String> getViewDefinition(final AlterViewStatement alterViewStatement) {
        if (alterViewStatement instanceof MySQLStatement) {
            return Optional.of(((MySQLAlterViewStatement) alterViewStatement).getViewDefinition());
        }
        if (alterViewStatement instanceof SQLServerStatement) {
            return Optional.of(((SQLServerAlterViewStatement) alterViewStatement).getViewDefinition());
        }
        return Optional.empty();
    }
    
    /**
     * Get rename view segment.
     *
     * @param alterViewStatement AlterViewStatement
     * @return rename view segment
     */
    public static Optional<SimpleTableSegment> getRenameView(final AlterViewStatement alterViewStatement) {
        if (alterViewStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLAlterViewStatement) alterViewStatement).getRenameView();
        }
        if (alterViewStatement instanceof OpenGaussStatement) {
            return ((OpenGaussAlterViewStatement) alterViewStatement).getRenameView();
        }
        return Optional.empty();
    }
}
