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

package org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.ddl;

import lombok.Getter;
import lombok.Setter;
import org.xyz.dbproxy.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.xyz.dbproxy.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;

import java.util.Optional;

/**
 * OpenGauss create schema statement.
 */
@Getter
@Setter
public final class OpenGaussCreateSchemaStatement extends CreateSchemaStatement implements OpenGaussStatement {
    
    private IdentifierValue username;
    
    /**
     * Get username.
     *
     * @return username
     */
    public Optional<IdentifierValue> getUsername() {
        return Optional.ofNullable(username);
    }
}
