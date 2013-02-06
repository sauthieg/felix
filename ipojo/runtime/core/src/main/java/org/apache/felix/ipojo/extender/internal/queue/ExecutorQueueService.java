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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.felix.ipojo.extender.internal.AbstractService;
import org.apache.felix.ipojo.extender.queue.Callback;
import org.apache.felix.ipojo.extender.queue.JobInfo;
import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.BundleContext;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 05/02/13
 * Time: 09:31
 * To change this template use File | Settings | File Templates.
 */
public class ExecutorQueueService extends AbstractService implements QueueService {

    private final static int DEFAULT_QUEUE_SIZE = 3;

    private final ExecutorService m_executorService;
    private final Statistic m_statistic = new Statistic();

    public ExecutorQueueService(BundleContext bundleContext) {
        this(bundleContext, DEFAULT_QUEUE_SIZE);
    }

    public ExecutorQueueService(BundleContext bundleContext, int size) {
        this(bundleContext, Executors.newFixedThreadPool(size));
    }

    public ExecutorQueueService(BundleContext bundleContext, int size, ThreadFactory threadFactory) {
        this(bundleContext, Executors.newFixedThreadPool(size, threadFactory));
    }

    // TODO Make this private so the ExecutorService instance is entirely managed by us
    public ExecutorQueueService(BundleContext bundleContext, ExecutorService executorService) {
        super(bundleContext, QueueService.class);
        m_executorService = executorService;
    }

    public void stop() {
        m_executorService.shutdown();
        super.stop();
    }

    @Override
    protected Dictionary<String, ?> getServiceProperties() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(QueueService.QUEUE_MODE_PROPERTY, QueueService.ASYNCHRONOUS_QUEUE_MODE);
        return properties;
    }

    public int getFinished() {
        return m_statistic.getFinishedCounter().get();
    }

    public int getWaiters() {
        return m_statistic.getWaiters().size();
    }

    public int getCurrents() {
        return m_statistic.getCurrentsCounter().get();
    }

    public List<JobInfo> getWaitersInfo() {
        List<JobInfo> snapshot;
        synchronized (m_statistic.getWaiters()) {
            snapshot = new ArrayList<JobInfo>(m_statistic.getWaiters());
        }
        return Collections.unmodifiableList(snapshot);
    }

    public <T> Future<T> submit(Callable<T> callable, Callback<T> callback, String description) {
        return m_executorService.submit(new JobInfoCallable<T>(m_statistic, callable, callback, description));
    }

    public <T> Future<T> submit(Callable<T> callable, String description) {
        return submit(callable, null, description);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return submit(callable, "No description");
    }

}
