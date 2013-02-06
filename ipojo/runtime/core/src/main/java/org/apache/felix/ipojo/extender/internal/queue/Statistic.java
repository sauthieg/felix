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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.extender.queue.JobInfo;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 09:19
 * To change this template use File | Settings | File Templates.
 */
public class Statistic {
    private final List<JobInfo> m_waiters = Collections.synchronizedList(new ArrayList<JobInfo>());
    private final AtomicInteger m_finished = new AtomicInteger(0);
    private final AtomicInteger m_currents = new AtomicInteger(0);

    public AtomicInteger getFinishedCounter() {
        return m_finished;
    }

    public List<JobInfo> getWaiters() {
        return m_waiters;
    }

    public AtomicInteger getCurrentsCounter() {
        return m_currents;
    }


}
