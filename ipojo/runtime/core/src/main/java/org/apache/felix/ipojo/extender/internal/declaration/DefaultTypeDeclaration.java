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
 * TODO Should be declarative only, the linking should be done by another entity.
 */
public class DefaultTypeDeclaration extends AbstractDeclaration implements TypeDeclaration, FactoryStateListener {

    private final BundleContext m_bundleContext;
    private final Element m_componentMetadata;

    private ServiceTracker m_extensionTracker;
    private ServiceTracker m_instanceTracker;
    private IPojoFactory m_factory;
    private ServiceRegistration<?> m_registration;
    private boolean frozen = false;
    private boolean visible = true;

    public DefaultTypeDeclaration(BundleContext bundleContext, Element componentMetadata) {
        m_bundleContext = bundleContext;
        m_componentMetadata = componentMetadata;
        String publicAttribute = componentMetadata.getAttribute("public");
        visible = (publicAttribute == null) || !publicAttribute.equalsIgnoreCase("false");
        try {
            initExtensionTracker();
            initInstanceTracker();
        } catch (InvalidSyntaxException e) {
            // Error during filter creation, froze the declaration and add a meaningful message
            frozen = true;
            unbind("Filter creation error", e);
        }
    }

    private void initExtensionTracker() throws InvalidSyntaxException {
        String filter = String.format(
                "(&(objectclass=%s)(%s=%s))",
                ExtensionDeclaration.class.getName(),
                ExtensionDeclaration.EXTENSION_NAME_PROPERTY,
                getQualifiedName()
        );
        m_extensionTracker = new ServiceTracker(m_bundleContext, m_bundleContext.createFilter(filter), new ExtensionSupport());
    }

    private void initInstanceTracker() throws InvalidSyntaxException {

        String filter;
        String version = getComponentVersion();
        if (version != null) {
            // Track instance for:
            // * this component AND
            // * this component's version OR no version
            filter = String.format(
                    "(&(objectClass=%s)(%s=%s)(|(%s=%s)(!(%s=*))))",
                    InstanceDeclaration.class.getName(),
                    InstanceDeclaration.COMPONENT_NAME_PROPERTY,
                    getComponentName(),
                    InstanceDeclaration.COMPONENT_VERSION_PROPERTY,
                    version,
                    InstanceDeclaration.COMPONENT_VERSION_PROPERTY
            );
        } else {
            // Track instance for:
            // * this component AND no version
            filter = String.format(
                    "(&(objectClass=%s)(%s=%s)(!(%s=*)))",
                    InstanceDeclaration.class.getName(),
                    InstanceDeclaration.COMPONENT_NAME_PROPERTY,
                    getComponentName(),
                    InstanceDeclaration.COMPONENT_VERSION_PROPERTY
            );
        }
        m_instanceTracker = new ServiceTracker(m_bundleContext, m_bundleContext.createFilter(filter), new InstanceSupport());
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
        return null;  //TODO To implement.
    }

    private String getQualifiedName() {
        if (m_componentMetadata.getNameSpace() == null) {
            return m_componentMetadata.getName();
        }
        return m_componentMetadata.getNameSpace() + ":" + m_componentMetadata.getName();
    }

    public void start() {
        m_registration = m_bundleContext.registerService(TypeDeclaration.class.getName(), this, null);
        if (!frozen) {
            m_extensionTracker.open(true);
        }
    }

    public void stop() {

        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }

        m_instanceTracker.close();
        m_extensionTracker.close();

    }

    public void stateChanged(Factory factory, int newState) {
        if (Factory.VALID == newState) {
            // Start tracking instances
            m_instanceTracker.open(true);
        } else {
            // Un-track all instances
            m_instanceTracker.close();
        }
    }

    private class ExtensionSupport implements ServiceTrackerCustomizer {
        public Object addingService(ServiceReference reference) {
            // TODO Check if we can cast the instance
            ExtensionDeclaration declaration = (ExtensionDeclaration) m_bundleContext.getService(reference);
            try {
                // Build and start the factory instance
                m_factory = declaration.getFactoryBuilder().build(m_bundleContext, m_componentMetadata);
                m_factory.addFactoryStateListener(DefaultTypeDeclaration.this);
                m_factory.start();

                // Change the status
                bind();

                return m_factory;
            } catch (FactoryBuilderException e) {
                unbind(String.format("Cannot build '%s' factory instance", getQualifiedName()), e);
            } catch (Throwable t) {
                unbind(String.format("Error during '%s' factory instance creation", getQualifiedName()), t);
            }

            return null;
        }

        public void modifiedService(ServiceReference reference, Object o) { }

        public void removedService(ServiceReference reference, Object o) {

            // Then stop the factory
            m_factory.stop();
            m_factory.removeFactoryStateListener(DefaultTypeDeclaration.this);
            m_factory = null;
            unbind("Extension '%s' is missing");
        }
    }

    private class InstanceSupport implements ServiceTrackerCustomizer {
        public Object addingService(ServiceReference reference) {
            // TODO Check if we can cast the instance
            InstanceDeclaration instanceDeclaration = (InstanceDeclaration) m_bundleContext.getService(reference);

            // Check that instance is not already bound
            if (instanceDeclaration.getStatus().isBound()) {
                return null;
            }

            // Handle visibility (private/public factories)
            if (!visible) {
                if (!reference.getBundle().equals(m_bundleContext.getBundle())) {
                    Bundle origin = m_bundleContext.getBundle();
                    instanceDeclaration.unbind(
                            String.format("Component '%s/%s' is private. It only accept instances from bundle %s/%s [%d] (instance bundle origin: %d)",
                                          getComponentName(),
                                          getComponentVersion(),
                                          origin.getSymbolicName(),
                                          origin.getVersion(),
                                          origin.getBundleId(),
                                          reference.getBundle().getBundleId())
                    );
                    return null;
                }
            }

            try {
                // Create the component's instance
                // It is automatically started
                ComponentInstance instance = m_factory.createComponentInstance(instanceDeclaration.getConfiguration());

                // Notify the declaration that everything is fine
                instanceDeclaration.bind();

                return instance;
            } catch (UnacceptableConfiguration c) {
                unbind(String.format("Instance configuration is invalid (component:%s/%s, bundle:%d)",
                                     getComponentName(),
                                     getComponentVersion(),
                                     reference.getBundle().getBundleId()),
                       c);
            } catch (MissingHandlerException e) {
                unbind(String.format("Component '%s/%s' is missing some handlers", getComponentName(), getComponentVersion()), e);
            } catch (ConfigurationException e) {
                unbind(String.format("Component '%s/%s' is incorrect", getComponentName(), getComponentVersion()), e);
            }

            return null;
        }

        public void modifiedService(ServiceReference reference, Object o) { }

        public void removedService(ServiceReference reference, Object o) {
            ComponentInstance instance = (ComponentInstance) o;
            instance.stop();
            instance.dispose();

            InstanceDeclaration instanceDeclaration = (InstanceDeclaration) m_bundleContext.getService(reference);

            String message;
            if (m_registration == null) {
                message = String.format("Factory for Component '%s/%s' is missing", getComponentName(), getComponentVersion());
            } else {
                message = String.format("Extension '%s' required by component '%s/%s' is missing", getQualifiedName(), getComponentName(), getComponentVersion());
            }
            instanceDeclaration.unbind(message);
        }
    }
}
