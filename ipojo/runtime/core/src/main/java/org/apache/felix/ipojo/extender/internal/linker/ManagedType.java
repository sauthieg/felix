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

package org.apache.felix.ipojo.extender.internal.linker;

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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 04/02/13
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class ManagedType implements FactoryStateListener {
    private final BundleContext m_bundleContext;
    private final TypeDeclaration m_declaration;
    private ServiceTracker m_extensionTracker;
    private ServiceTracker m_instanceTracker;
    private IPojoFactory m_factory;
    private boolean m_frozen;

    public ManagedType(BundleContext bundleContext, TypeDeclaration declaration) {
        m_bundleContext = bundleContext;
        m_declaration = declaration;
        try {
            initExtensionTracker();
            initInstanceTracker();
        } catch (InvalidSyntaxException e) {
            // Error during filter creation, froze the declaration and add a meaningful message
            m_frozen = true;
            m_declaration.unbind("Filter creation error", e);
        }
    }

    private void initExtensionTracker() throws InvalidSyntaxException {
        String filter = String.format(
                "(&(objectclass=%s)(%s=%s))",
                ExtensionDeclaration.class.getName(),
                ExtensionDeclaration.EXTENSION_NAME_PROPERTY,
                m_declaration.getExtension()
        );
        m_extensionTracker = new ServiceTracker(m_bundleContext, m_bundleContext.createFilter(filter), new ExtensionSupport());
    }

    private void initInstanceTracker() throws InvalidSyntaxException {

        String filter;
        String version = m_declaration.getComponentVersion();
        if (version != null) {
            // Track instance for:
            // * this component AND
            // * this component's version OR no version
            filter = String.format(
                    "(&(objectClass=%s)(%s=%s)(|(%s=%s)(!(%s=*))))",
                    InstanceDeclaration.class.getName(),
                    InstanceDeclaration.COMPONENT_NAME_PROPERTY,
                    m_declaration.getComponentName(),
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
                    m_declaration.getComponentName(),
                    InstanceDeclaration.COMPONENT_VERSION_PROPERTY
            );
        }
        m_instanceTracker = new ServiceTracker(m_bundleContext, m_bundleContext.createFilter(filter), new InstanceSupport());
    }

    public void start() {
        if (!m_frozen) {
            m_extensionTracker.open(true);
        }
    }

    public void stop() {
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
                m_factory = declaration.getFactoryBuilder().build(m_bundleContext, m_declaration.getComponentMetadata());
                m_factory.addFactoryStateListener(ManagedType.this);
                m_factory.start();

                // Change the status
                m_declaration.bind();

                return m_factory;
            } catch (FactoryBuilderException e) {
                m_declaration.unbind(String.format("Cannot build '%s' factory instance", m_declaration.getExtension()), e);
            } catch (Throwable t) {
                m_declaration.unbind(String.format("Error during '%s' factory instance creation", m_declaration.getExtension()), t);
            }

            return null;
        }

        public void modifiedService(ServiceReference reference, Object o) { }

        public void removedService(ServiceReference reference, Object o) {

            // Then stop the factory
            m_factory.stop();
            m_factory.removeFactoryStateListener(ManagedType.this);
            m_factory = null;
            m_declaration.unbind("Extension '%s' is missing");
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
            if (!m_declaration.isPublic()) {
                if (!reference.getBundle().equals(m_bundleContext.getBundle())) {
                    Bundle origin = m_bundleContext.getBundle();
                    instanceDeclaration.unbind(
                            String.format("Component '%s/%s' is private. It only accept instances from bundle %s/%s [%d] (instance bundle origin: %d)",
                                    m_declaration.getComponentName(),
                                    m_declaration.getComponentVersion(),
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
                m_declaration.unbind(String.format("Instance configuration is invalid (component:%s/%s, bundle:%d)",
                        m_declaration.getComponentName(),
                        m_declaration.getComponentVersion(),
                        reference.getBundle().getBundleId()),
                        c);
            } catch (MissingHandlerException e) {
                m_declaration.unbind(String.format("Component '%s/%s' is missing some handlers", m_declaration.getComponentName(), m_declaration.getComponentVersion()), e);
            } catch (ConfigurationException e) {
                m_declaration.unbind(String.format("Component '%s/%s' is incorrect", m_declaration.getComponentName(), m_declaration.getComponentVersion()), e);
            }

            return null;
        }

        public void modifiedService(ServiceReference reference, Object o) { }

        public void removedService(ServiceReference reference, Object o) {
            ComponentInstance instance = (ComponentInstance) o;
            instance.stop();
            instance.dispose();

            InstanceDeclaration instanceDeclaration = (InstanceDeclaration) m_bundleContext.getService(reference);

            String message = String.format("Factory for Component '%s/%s' is missing",
                                           m_declaration.getComponentName(),
                                           m_declaration.getComponentVersion());
            instanceDeclaration.unbind(message);
        }
    }


}
