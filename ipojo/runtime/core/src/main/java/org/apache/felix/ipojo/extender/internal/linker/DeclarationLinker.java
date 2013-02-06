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

import org.apache.felix.ipojo.extender.TypeDeclaration;
import org.apache.felix.ipojo.extender.queue.QueueService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 04/02/13
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class DeclarationLinker implements ServiceTrackerCustomizer {
    private final BundleContext m_bundleContext;
    private final QueueService m_queueService;
    private final ServiceTracker m_typeTracker;

    public DeclarationLinker(BundleContext bundleContext, QueueService queueService) {
        m_bundleContext = bundleContext;
        m_queueService = queueService;
        m_typeTracker = new ServiceTracker(m_bundleContext, TypeDeclaration.class.getName(), this);
   }

    public void start() {
        m_typeTracker.open(true);
    }

    public void stop() {
        m_typeTracker.close();
    }

    public Object addingService(ServiceReference reference) {
        TypeDeclaration declaration = (TypeDeclaration) m_bundleContext.getService(reference);
        ManagedType managedType = new ManagedType(reference.getBundle().getBundleContext(), m_queueService, declaration);
        managedType.start();
        return managedType;
    }

    public void modifiedService(ServiceReference reference, Object service) {
        // Ignored
    }

    public void removedService(ServiceReference reference, Object service) {
        ManagedType managedType = (ManagedType) service;
        managedType.stop();
    }
}
