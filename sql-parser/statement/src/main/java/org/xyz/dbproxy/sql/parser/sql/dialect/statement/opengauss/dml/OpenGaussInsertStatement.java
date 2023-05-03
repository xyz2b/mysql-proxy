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

package org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.dml;

import lombok.Setter;
import org.xyz.dbproxy.sql.parser.sql.common.segment.dml.ReturningSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.xyz.dbproxy.sql.parser.sql.common.segment.generic.WithSegment;
import org.xyz.dbproxy.sql.parser.sql.common.statement.dml.InsertStatement;
import org.xyz.dbproxy.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;

import java.util.Optional;

/**
 * OpenGauss insert statement.
 */
@Setter
public final class OpenGaussInsertStatement extends InsertStatement implements OpenGaussStatement {
    
    private WithSegment withSegment;
    
    private OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment;
    
    private ReturningSegment returningSegment;
    
    /**
     * Get with segment.
     *
     * @return with segment.
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }
    
    /**
     * Get on duplicate key columns segment.
     *
     * @return on duplicate key columns segment
     */
    public Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
        return Optional.ofNullable(onDuplicateKeyColumnsSegment);
    }
    
    /**
     * Get returning segment.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturningSegment() {
        return Optional.ofNullable(returningSegment);
    }
}
