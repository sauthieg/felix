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

package org.apache.felix.ipojo.extender.internal;

import java.util.Dictionary;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 05/02/13
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractService {

    private final BundleContext m_bundleContext;
    private final Class<?> m_type;
    private ServiceRegistration<?> m_registration;


    protected AbstractService(BundleContext bundleContext, Class<?> type) {
        m_bundleContext = bundleContext;
        if (!type.isAssignableFrom(getClass())) {
            throw new IllegalArgumentException("This object is not an instance of " + type.getName());
        }
        m_type = type;
    }

    public void start() {
        m_registration = m_bundleContext.registerService(m_type.getName(), this, getServiceProperties());
    }

    public void stop() {
        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }
    }

    protected Dictionary<String, ?> getServiceProperties() {
        return null;
    }
}
