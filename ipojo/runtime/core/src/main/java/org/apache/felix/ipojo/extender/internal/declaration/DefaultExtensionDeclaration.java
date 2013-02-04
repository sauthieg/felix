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

package org.apache.felix.ipojo.extender.internal.declaration;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.builder.FactoryBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Default implementation of the iPOJO Extension Declaration.
 */
public class DefaultExtensionDeclaration extends AbstractDeclaration implements ExtensionDeclaration {

    private final BundleContext m_bundleContext;
    private final FactoryBuilder m_factoryBuilder;
    private final String m_type;
    private ServiceRegistration<?> m_registration;

    public DefaultExtensionDeclaration(BundleContext bundleContext, FactoryBuilder factoryBuilder, String type) {
        m_bundleContext = bundleContext;
        m_factoryBuilder = factoryBuilder;
        m_type = type;
    }

    public FactoryBuilder getFactoryBuilder() {
        return m_factoryBuilder;
    }

    public String getExtensionName() {
        return m_type;
    }

    public void start() {
        m_registration = m_bundleContext.registerService(ExtensionDeclaration.class.getName(), this, getServiceProperties());
        bind();
    }

    private Dictionary<String, ?> getServiceProperties() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ExtensionDeclaration.EXTENSION_NAME_PROPERTY, m_type);
        return properties;
    }

    public void stop() {
        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }
    }

}
