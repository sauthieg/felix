/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.felix.ipojo.extender.internal.queue;

import org.apache.felix.ipojo.extender.internal.queue.callable.StringCallable;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 09:27
 * To change this template use File | Settings | File Templates.
 */
public class JobInfoCallableTestCase extends TestCase {
    public void testCall() throws Exception {
        Statistic stat = new Statistic();
        long mark = System.currentTimeMillis();
        JobInfoCallable<String> info = new JobInfoCallable<String>(stat, new StringCallable(), null, null);

        // Before execution
        assertTrue(info.getEnlistmentTime() >= mark);
        assertEquals(-1, info.getExecutionDuration());
        assertTrue(info.getWaitDuration() <= (System.currentTimeMillis() - mark));

        assertTrue(stat.getWaiters().contains(info));
        assertEquals(0, stat.getCurrentsCounter().get());
        assertEquals(0, stat.getFinishedCounter().get());

        info.call();

        assertTrue(info.getExecutionDuration() != -1);

        assertTrue(stat.getWaiters().isEmpty());
        assertEquals(0, stat.getCurrentsCounter().get());
        assertEquals(1, stat.getFinishedCounter().get());

    }
}
