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

package org.apache.felix.ipojo.extender.internal.queue.pref;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.extender.queue.Callback;
import org.apache.felix.ipojo.extender.queue.JobInfo;
import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
public class PreferenceQueueService implements QueueService {

    private final PreferenceSelection m_strategy;
    private final QueueService m_syncQueue;
    private final QueueService m_asyncQueue;

    private QueueService m_defaultQueue;

    public PreferenceQueueService(PreferenceSelection strategy, QueueService syncQueue, QueueService asyncQueue) {
        m_strategy = strategy;
        m_syncQueue = syncQueue;
        m_asyncQueue = asyncQueue;

        // By default, system queue is asynchronous
        m_defaultQueue = asyncQueue;
    }

    public int getFinished() {
        return m_syncQueue.getFinished() + m_asyncQueue.getFinished();
    }

    public int getWaiters() {
        return m_syncQueue.getWaiters() + m_asyncQueue.getWaiters();
    }

    public int getCurrents() {
        return m_syncQueue.getCurrents() + m_asyncQueue.getCurrents();
    }

    public List<JobInfo> getWaitersInfo() {
        // synchronous queue as no waiters, so snapshot is always empty and can be ignored
        return m_asyncQueue.getWaitersInfo();
    }

    public <T> Future<T> submit(Callable<T> callable, Callback<T> callback, String description) {
        // Argghhh, how can I choose between the 2 QueueService ?
        // I was expecting to have the source Bundle to make a decision
        Preference preference = Preference.DEFAULT;
        if (callable instanceof BundleReference) {
            Bundle bundle = ((BundleReference) callable).getBundle();
            preference = m_strategy.select(bundle);
        }

        QueueService selected = m_defaultQueue;
        switch (preference) {
            case ASYNC:
                selected = m_asyncQueue;
                break;
            case SYNC:
                selected = m_syncQueue;
                break;
        }

        return selected.submit(callable, callback, description);
    }

    public <T> Future<T> submit(Callable<T> callable, String description) {
        return submit(callable, null, description);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return submit(callable, "No description");
    }
}
