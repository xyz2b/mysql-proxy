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

package org.xyz.dbproxy.sql.parser.sql.common.enums;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalOperatorTest {
    
    @Test
    void assertValueFromAndText() {
        assertTrue(LogicalOperator.valueFrom("AND").isPresent());
        assertThat(LogicalOperator.valueFrom("AND").get(), CoreMatchers.is(LogicalOperator.AND));
        assertTrue(LogicalOperator.valueFrom("and").isPresent());
        assertThat(LogicalOperator.valueFrom("and").get(), CoreMatchers.is(LogicalOperator.AND));
    }
    
    @Test
    void assertValueFromAndSymbol() {
        assertTrue(LogicalOperator.valueFrom("&&").isPresent());
        assertThat(LogicalOperator.valueFrom("&&").get(), CoreMatchers.is(LogicalOperator.AND));
    }
    
    @Test
    void assertValueFromOrText() {
        assertTrue(LogicalOperator.valueFrom("OR").isPresent());
        assertThat(LogicalOperator.valueFrom("OR").get(), CoreMatchers.is(LogicalOperator.OR));
        assertTrue(LogicalOperator.valueFrom("or").isPresent());
        assertThat(LogicalOperator.valueFrom("or").get(), CoreMatchers.is(LogicalOperator.OR));
    }
    
    @Test
    void assertValueFromOrSymbol() {
        assertTrue(LogicalOperator.valueFrom("||").isPresent());
        assertThat(LogicalOperator.valueFrom("||").get(), CoreMatchers.is(LogicalOperator.OR));
    }
    
    @Test
    void assertValueFromInvalidValue() {
        assertFalse(LogicalOperator.valueFrom("XX").isPresent());
    }
}
