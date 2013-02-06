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

package org.apache.felix.ipojo.extender.queue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 05/02/13
 * Time: 09:21
 * To change this template use File | Settings | File Templates.
 */
public interface QueueService {
    /**
     * The service property specifying the queue mode (sync/async/pref).
     */
    String QUEUE_MODE_PROPERTY = "ipojo.queue.mode";

    /**
     * Synchronous queue mode.
     */
    String SYNCHRONOUS_QUEUE_MODE = "sync";

    /**
     * Asynchronous queue mode.
     */
    String ASYNCHRONOUS_QUEUE_MODE = "async";

    /**
     * Preference queue mode.
     */
    String PREFERENCE_QUEUE_MODE = "pref";

    /**
     * The service property specifying the queue scope (global/...).
     */
    String QUEUE_SCOPE_PROPERTY = "ipojo.queue.scope";

    /**
     * Global queue scope.
     */
    String GLOABL_QUEUE_SCOPE = "global";

    /**
     * @return the number of jobs that have been executed entirely
     * (including successful and erroneous jobs).
     */
    int getFinished();

    /**
     * @return the number of jobs scheduled but not yet started.
     */
    int getWaiters();

    /**
     * @return the number of jobs currently executed (started but not finished).
     */
    int getCurrents();

    /**
     * @return a snapshot of the currently waiting jobs.
     */
    List<JobInfo> getWaitersInfo();

    // Note: I don't want us to store error reports there
    // Maybe we should use EventAdmin to send notifications ?
    // getErrors

    <T> Future<T> submit(Callable<T> callable, Callback<T> callback, String description);
    <T> Future<T> submit(Callable<T> callable, String description);
    <T> Future<T> submit(Callable<T> callable);


    // TODO Add a way to add global callbacks
    //<T> void addGlobalCallback(Callback<T> callback);
    // <T> void removeGlobalCallback(Callback<T> callback);
}
