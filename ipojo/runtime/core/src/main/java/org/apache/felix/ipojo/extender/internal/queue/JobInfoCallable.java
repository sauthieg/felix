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

import java.util.concurrent.Callable;

import org.apache.felix.ipojo.extender.queue.Callback;
import org.apache.felix.ipojo.extender.queue.JobInfo;

/**
* Created with IntelliJ IDEA.
* User: guillaume
* Date: 06/02/13
* Time: 09:23
* To change this template use File | Settings | File Templates.
*/
public class JobInfoCallable<T> implements Callable<T>, JobInfo {

    private final Statistic m_statistic;
    private final Callable<T> m_delegate;
    private final Callback<T> m_callback;
    private final String m_description;

    private long enlistmentTime = System.currentTimeMillis();
    private long startTime = -1;
    private long endTime = -1;

    public JobInfoCallable(Statistic statistic,
                           Callable<T> delegate,
                           Callback<T> callback,
                           String description) {
        m_statistic = statistic;
        m_delegate = delegate;
        m_callback = callback;
        m_description = description;
        m_statistic.getWaiters().add(this);
    }

    public T call() throws Exception {
        m_statistic.getWaiters().remove(this);
        startTime = System.currentTimeMillis();
        m_statistic.getCurrentsCounter().incrementAndGet();
        T result = null;
        try {
            result = m_delegate.call();
            return result;
        } catch (Exception e) {
            if (m_callback != null) {
                m_callback.error(this, e);
            }
            throw e;
        } finally {
            m_statistic.getCurrentsCounter().decrementAndGet();
            m_statistic.getFinishedCounter().incrementAndGet();
            endTime = System.currentTimeMillis();
            if (m_callback != null) {
                m_callback.success(this, result);
            }
        }
    }

    public long getEnlistmentTime() {
        return enlistmentTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getWaitDuration() {
        long end = startTime;
        if (end == -1) {
            // Not yet started
            // Still waiting
            end = System.currentTimeMillis();
        }
        return end - enlistmentTime;
    }

    public long getExecutionDuration() {
        if ((startTime == -1) || (endTime == -1)) {
            return -1;
        }
        return endTime - startTime;
    }

    public String getDescription() {
        return m_description;
    }
}
