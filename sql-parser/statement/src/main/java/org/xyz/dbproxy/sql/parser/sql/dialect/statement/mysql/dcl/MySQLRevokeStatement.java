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

package org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.dcl;

import lombok.Getter;
import lombok.Setter;
import org.xyz.dbproxy.sql.parser.sql.common.segment.generic.ACLTypeEnum;
import org.xyz.dbproxy.sql.parser.sql.common.segment.generic.GrantLevelSegment;
import org.xyz.dbproxy.sql.parser.sql.common.statement.dcl.RevokeStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.segment.MySQLRoleOrPrivilegeSegment;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * MySQL revoke statement.
 */
@Getter
@Setter
public final class MySQLRevokeStatement extends RevokeStatement implements MySQLStatement {
    
    private final Collection<MySQLRoleOrPrivilegeSegment> roleOrPrivileges = new LinkedList<>();
    
    private boolean allPrivileges;
    
    private UserSegment onUser;
    
    private final Collection<UserSegment> fromUsers = new LinkedList<>();
    
    private ACLTypeEnum aclType;
    
    private GrantLevelSegment level;
}
