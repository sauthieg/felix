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

import java.util.LinkedList;

import org.apache.felix.ipojo.extender.internal.BundleProcessor;
import org.osgi.framework.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 06/02/13
 * Time: 22:16
 * To change this template use File | Settings | File Templates.
 */
public class ReverseBundleProcessor extends ForwardingBundleProcessor {

    private final BundleProcessor m_delegate;
    private LinkedList<Bundle> m_bundles = new LinkedList<Bundle>();

    public ReverseBundleProcessor(BundleProcessor delegate) {
        m_delegate = delegate;
    }

    @Override
    protected BundleProcessor delegate() {
        return m_delegate;
    }

    @Override
    public void activate(Bundle bundle) {
        m_bundles.addLast(bundle);
        super.activate(bundle);
    }

    @Override
    public void deactivate(Bundle bundle) {
        m_bundles.remove(bundle);
        super.deactivate(bundle);
    }

    @Override
    public void stop() {
        // deactivate in reverse order
        while (!m_bundles.isEmpty()) {
            super.deactivate(m_bundles.pollLast());
        }
        super.stop();
    }
}
