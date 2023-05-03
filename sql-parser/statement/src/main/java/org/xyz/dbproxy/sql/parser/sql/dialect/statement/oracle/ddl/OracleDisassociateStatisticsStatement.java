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

package org.xyz.dbproxy.sql.parser.sql.dialect.statement.oracle.ddl;

import lombok.Getter;
import lombok.Setter;
import org.xyz.dbproxy.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.ddl.index.IndexTypeSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.ddl.packages.PackageSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.ddl.type.TypeSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.xyz.dbproxy.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.xyz.dbproxy.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.oracle.OracleStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Oracle disassociate statistics statement.
 */
@Getter
@Setter
public final class OracleDisassociateStatisticsStatement extends AbstractSQLStatement implements DDLStatement, OracleStatement {
    
    private List<IndexSegment> indexes = new LinkedList<>();
    
    private List<SimpleTableSegment> tables = new LinkedList<>();
    
    private List<ColumnSegment> columns = new LinkedList<>();
    
    private List<FunctionSegment> functions = new LinkedList<>();
    
    private List<PackageSegment> packages = new LinkedList<>();
    
    private List<TypeSegment> types = new LinkedList<>();
    
    private List<IndexTypeSegment> indexTypes = new LinkedList<>();
}