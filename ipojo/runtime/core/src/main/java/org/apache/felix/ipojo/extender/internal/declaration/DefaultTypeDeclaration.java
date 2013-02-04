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

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.FactoryStateListener;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.InstanceDeclaration;
import org.apache.felix.ipojo.extender.TypeDeclaration;
import org.apache.felix.ipojo.extender.builder.FactoryBuilderException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Default implementation of the component type declaration.
 */
public class DefaultTypeDeclaration extends AbstractDeclaration implements TypeDeclaration {

    private final BundleContext m_bundleContext;
    private final Element m_componentMetadata;
    private ServiceRegistration<?> m_registration;
    private boolean visible = true;

    public DefaultTypeDeclaration(BundleContext bundleContext, Element componentMetadata) {
        m_bundleContext = bundleContext;
        m_componentMetadata = componentMetadata;
        String publicAttribute = componentMetadata.getAttribute("public");
        visible = (publicAttribute == null) || !publicAttribute.equalsIgnoreCase("false");
    }

    public String getComponentName() {
        String name = m_componentMetadata.getAttribute("name");
        if (name == null) {
            name = m_componentMetadata.getAttribute("classname");
        }
        return name;
    }

    public String getComponentVersion() {
        String version = m_componentMetadata.getAttribute("version");
        if (version != null) {
            if ("bundle".equalsIgnoreCase(version)) {
                return m_bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
            }
        }
        return version;
    }

    public String getExtension() {
        if (m_componentMetadata.getNameSpace() == null) {
            return m_componentMetadata.getName();
        }
        return m_componentMetadata.getNameSpace() + ":" + m_componentMetadata.getName();
    }

    public Element getComponentMetadata() {
        return m_componentMetadata;
    }

    public boolean isPublic() {
        return visible;
    }

    public void start() {
        m_registration = m_bundleContext.registerService(TypeDeclaration.class.getName(), this, null);
    }

    public void stop() {
        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }
    }
}
