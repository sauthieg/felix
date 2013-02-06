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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.felix.ipojo.extender.internal.LifecycleQueueService;
import org.apache.felix.ipojo.extender.internal.queue.pref.Preference;
import org.apache.felix.ipojo.extender.internal.queue.pref.PreferenceSelection;
import org.apache.felix.ipojo.extender.queue.Callback;
import org.apache.felix.ipojo.extender.queue.QueueService;
import org.apache.felix.ipojo.util.Log;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 10:54
 * To change this template use File | Settings | File Templates.
 */
public class EnforcedQueueService extends ForwardingQueueService {

    private final PreferenceSelection m_strategy;
    private final LifecycleQueueService m_queueService;
    private final Preference m_enforced;
    private final Log m_logger;

    public EnforcedQueueService(PreferenceSelection strategy, LifecycleQueueService queueService, Preference enforced, Log logger) {
        m_strategy = strategy;
        m_queueService = queueService;
        m_enforced = enforced;
        m_logger = logger;
    }

    @Override
    protected LifecycleQueueService delegate() {
        return m_queueService;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable, Callback<T> callback, String description) {
        checkBundlePreference(callable);
        return super.submit(callable, callback, description);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable, String description) {
        checkBundlePreference(callable);
        return super.submit(callable, description);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        checkBundlePreference(callable);
        return super.submit(callable);
    }

    private void checkBundlePreference(Callable<?> callable) {
        if (callable instanceof BundleReference) {
            Bundle bundle = ((BundleReference) callable).getBundle();
            Preference preference = m_strategy.select(bundle);

            if (!isCompatible(preference)) {
                // Log a warning, Bundle asked for a synchronous processing,
                // but we will enforce parametrised processing
                String message = String.format(
                        "Enforcing %s mode for Bundle %s/%s [%d] (asking for %s)",
                        m_enforced.name(),
                        bundle.getSymbolicName(),
                        bundle.getVersion(),
                        bundle.getBundleId(),
                        preference
                );
                m_logger.log(Logger.WARNING, message);
            }
        }
    }

    private boolean isCompatible(Preference preference) {
        return ((preference == m_enforced) || (preference == Preference.DEFAULT));
    }
}
