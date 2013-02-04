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

import org.apache.felix.ipojo.extender.InstanceDeclaration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Default implementation of the instance declaration.
 */
public class DefaultInstanceDeclaration extends AbstractDeclaration implements InstanceDeclaration {

    private static final Dictionary<String, Object> EMPTY_DICTIONARY = new Hashtable<String, Object>();

    private final BundleContext m_bundleContext;
    private final String m_componentName;
    private final Dictionary<String, Object> m_configuration;

    private ServiceRegistration<?> m_registration;

    public DefaultInstanceDeclaration(BundleContext bundleContext, String componentName) {
        this(bundleContext, componentName, EMPTY_DICTIONARY);
    }

    public DefaultInstanceDeclaration(BundleContext bundleContext, String componentName, Dictionary<String, Object> configuration) {
        m_bundleContext = bundleContext;
        m_componentName = componentName;
        m_configuration = configuration;
    }

    public Dictionary<String, Object> getConfiguration() {
        return m_configuration;
    }

    public String getComponentName() {
        return m_componentName;
    }

    public String getComponentVersion() {
        return (String) m_configuration.get("factory.version"); // TODO Extract factory.version as constant
    }

    public String getInstanceName() {
        String name = (String) m_configuration.get("instance.name");   // TODO Extract instance.name as constant
        if (name == null) {
            return UNNAMED_INSTANCE;
        }
        return name;
    }

    public void start() {
        m_registration = m_bundleContext.registerService(InstanceDeclaration.class.getName(), this, getServiceProperties());
    }

    private Dictionary<String, ?> getServiceProperties() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(InstanceDeclaration.COMPONENT_NAME_PROPERTY, m_componentName);

        String version = getComponentVersion();
        if (version != null) {
            properties.put(InstanceDeclaration.COMPONENT_VERSION_PROPERTY, version);
        }

        String name = getInstanceName();
        if (name != null) {
            properties.put(InstanceDeclaration.INSTANCE_NAME, name);
        } else {
            properties.put(InstanceDeclaration.INSTANCE_NAME, UNNAMED_INSTANCE);
        }

        return properties;
    }

    public void stop() {
        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }
    }

}
