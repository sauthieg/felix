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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.internal.Extender;
import org.apache.felix.ipojo.util.Logger;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import junit.framework.TestCase;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 02/02/13
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class ExtensionBundleProcessorTestCase extends TestCase {

    @Mock
    private BundleContext m_bundleContext;

    private Logger m_logger;

    @Mock
    private Bundle m_bundle;

    @Override
    public void setUp() throws Exception {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        MockitoAnnotations.initMocks(this);

        when(m_bundle.getBundleContext()).thenReturn(m_bundleContext);
        when(m_bundleContext.getBundle()).thenReturn(m_bundle);
        when(m_bundle.getHeaders()).thenReturn(headers);

        m_logger = new Logger(m_bundleContext, "test");
    }

    public void testEmptyExtensionBundle() throws Exception {
        ExtensionBundleProcessor processor = new ExtensionBundleProcessor(m_logger);
        processor.activate(m_bundle);
        verify(m_bundleContext, never()).registerService((Class<?>) null, null, null);
    }

    public void testSimpleExtensionBundle() throws Exception {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(Extender.IPOJO_EXTENSION, "component:"+ ComponentFactory.class.getName());
        when(m_bundle.getHeaders()).thenReturn(headers);
        Mockito.<Class<?>>when(m_bundle.loadClass(anyString())).thenReturn(ComponentFactory.class);

        ExtensionBundleProcessor processor = new ExtensionBundleProcessor(m_logger);
        processor.activate(m_bundle);
        verify(m_bundleContext).registerService(eq(ExtensionDeclaration.class.getName()), any(ExtensionDeclaration.class), any(Dictionary.class));
    }

}
