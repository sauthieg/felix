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

package org.apache.felix.ipojo.extender.internal.processor;

import org.apache.felix.ipojo.extender.internal.BundleProcessor;
import org.apache.felix.ipojo.extender.internal.ReferenceableCallable;
import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */
public class QueuingActivationProcessor extends ForwardingBundleProcessor {
    private final BundleProcessor m_delegate;
    private final QueueService m_queueService;

    public QueuingActivationProcessor(BundleProcessor delegate, QueueService queueService) {
        m_delegate = delegate;
        m_queueService = queueService;
    }

    @Override
    protected BundleProcessor delegate() {
        return m_delegate;
    }

    public void activate(final Bundle bundle) {
        m_queueService.submit(new ReferenceableCallable<Boolean>(bundle) {
            public Boolean call() throws Exception {
                QueuingActivationProcessor.super.activate(bundle);
                return true;
            }
        });
    }

}
