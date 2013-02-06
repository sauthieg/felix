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

package org.apache.felix.ipojo.extender.internal.queue.pref.enforce;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.extender.internal.LifecycleQueueService;
import org.apache.felix.ipojo.extender.queue.Callback;
import org.apache.felix.ipojo.extender.queue.JobInfo;
import org.apache.felix.ipojo.extender.queue.QueueService;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 10:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class ForwardingQueueService implements LifecycleQueueService {

    protected abstract LifecycleQueueService delegate();

    public void start() {
        delegate().start();
    }

    public void stop() {
        delegate().stop();
    }

    public int getFinished() {
        return delegate().getFinished();
    }

    public int getWaiters() {
        return delegate().getWaiters();
    }

    public int getCurrents() {
        return delegate().getCurrents();
    }

    public List<JobInfo> getWaitersInfo() {
        return delegate().getWaitersInfo();
    }

    public <T> Future<T> submit(Callable<T> callable, Callback<T> callback, String description) {
        return delegate().submit(callable, callback, description);
    }

    public <T> Future<T> submit(Callable<T> callable, String description) {
        return delegate().submit(callable, description);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return delegate().submit(callable);
    }
}
