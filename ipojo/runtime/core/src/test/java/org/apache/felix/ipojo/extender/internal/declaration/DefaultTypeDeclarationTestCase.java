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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.FactoryStateListener;
import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.TypeDeclaration;
import org.apache.felix.ipojo.extender.builder.FactoryBuilder;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 01/02/13
 * Time: 13:21
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTypeDeclarationTestCase extends TestCase {

    @Mock
    private BundleContext m_bundleContext;

    @Mock
    private Filter filter;

    @Mock
    private ServiceReference extensionReference;

    @Mock
    private ExtensionDeclaration m_extension;

    @Mock
    private FactoryBuilder m_builder;

    @Mock
    private IPojoFactory factory;

    @Captor
    private ArgumentCaptor<ServiceListener> captor;

    @Captor
    private ArgumentCaptor<FactoryStateListener> fslCaptor;

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testRegistration() throws Exception {
        when(m_bundleContext.createFilter(anyString())).thenReturn(filter);

        DefaultTypeDeclaration declaration = new DefaultTypeDeclaration(m_bundleContext, element("component", "component.Hello"));
        declaration.start();

        // Declaration is not bound
        assertFalse(declaration.getStatus().isBound());

        // Verify service registration
        verify(m_bundleContext).registerService(TypeDeclaration.class.getName(), declaration, null);

    }

    public void testActivationDeactivation() throws Exception {
        when(m_bundleContext.createFilter(anyString())).thenReturn(filter);
        when(filter.match(extensionReference)).thenReturn(true);
        when(m_bundleContext.getService(extensionReference)).thenReturn(m_extension);
        when(m_extension.getFactoryBuilder()).thenReturn(m_builder);
        when(m_builder.build(any(BundleContext.class), any(Element.class))).thenReturn(factory);

        DefaultTypeDeclaration declaration = new DefaultTypeDeclaration(m_bundleContext, element("component", "component.Hello"));
        declaration.start();

        // Declaration is not bound
        assertFalse(declaration.getStatus().isBound());

        verify(m_bundleContext).addServiceListener(captor.capture(), anyString());

        ServiceListener listener = captor.getValue();
        ServiceEvent e = new ServiceEvent(ServiceEvent.REGISTERED, extensionReference);
        listener.serviceChanged(e);

        verify(factory).addFactoryStateListener(fslCaptor.capture());
        FactoryStateListener fsl = fslCaptor.getValue();
        fsl.stateChanged(factory, Factory.VALID);

        assertTrue(declaration.getStatus().isBound());

        // The 2nd tracker should have registered its own listener
        verify(m_bundleContext, times(2)).addServiceListener(captor.capture(), anyString());
        ServiceListener listener2 = captor.getValue();
        assertNotSame(listener, listener2);

        ServiceEvent e2 = new ServiceEvent(ServiceEvent.UNREGISTERING, extensionReference);
        listener.serviceChanged(e2);

        // After extension removal, the declaration should be unbound
        assertFalse(declaration.getStatus().isBound());
    }

    private Element element(String type, String name) {
        Element root = new Element(type, null);
        root.addAttribute(new Attribute("name", name));
        return root;
    }
}
