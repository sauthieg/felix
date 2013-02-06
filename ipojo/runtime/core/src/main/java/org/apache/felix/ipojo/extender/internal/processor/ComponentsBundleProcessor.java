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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.extender.internal.BundleProcessor;
import org.apache.felix.ipojo.extender.internal.Extender;
import org.apache.felix.ipojo.extender.internal.declaration.DefaultInstanceDeclaration;
import org.apache.felix.ipojo.extender.internal.declaration.DefaultTypeDeclaration;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ManifestMetadataParser;
import org.apache.felix.ipojo.parser.ParseException;
import org.apache.felix.ipojo.util.Log;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 31/01/13
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
public class ComponentsBundleProcessor implements BundleProcessor {
    private final Log m_logger;
    private final Map<Bundle, ComponentsAndInstances> m_registry = new HashMap<Bundle, ComponentsAndInstances>();

    public ComponentsBundleProcessor(Log logger) {
        m_logger = logger;
    }

    public void activate(Bundle bundle) {
        Dictionary dict = bundle.getHeaders();
        // Check bundle
        String header = (String) dict.get(Extender.IPOJO_HEADER);
        // Check the alternative header
        if (header == null) {
            header = (String) dict.get(Extender.IPOJO_HEADER_ALT);
        }

        if (header != null) {
            try {
                parse(bundle, header);
            } catch (IOException e) {
                m_logger.log(Logger.ERROR, "An exception occurs during the parsing of the bundle " + bundle.getBundleId(), e);
            } catch (ParseException e) {
                m_logger.log(Logger.ERROR, "A parse exception occurs during the parsing of the bundle " + bundle.getBundleId(), e);
            }
        }

    }

    public void deactivate(Bundle bundle) {
        ComponentsAndInstances cai = m_registry.remove(bundle);
        if (cai != null) {
            cai.stop();
        }
    }

    public void start() {
        // Nothing to do
    }

    public void stop() {
        // Construct a new instance to avoid ConcurrentModificationException since deactivate also change the list
        // Sort the list greater first, so last installed bundles are deactivated first
        List<Bundle> bundles = new ArrayList<Bundle>(m_registry.keySet());
        Collections.sort(bundles, Collections.reverseOrder());
        for (Bundle bundle : bundles) {
            deactivate(bundle);
        }
    }

    /**
     * Parses the internal metadata (from the manifest
     * (in the iPOJO-Components property)). This methods
     * creates factories and add instances to the instance creator.
     * @param bundle the owner bundle.
     * @param components The iPOJO Header String.
     * @throws IOException if the manifest can not be found
     * @throws ParseException if the parsing process failed
     */
    private void parse(Bundle bundle, String components) throws IOException, ParseException {
        ManifestMetadataParser parser = new ManifestMetadataParser();
        parser.parseHeader(components);

        // Get the component type declaration
        Element[] metadata = parser.getComponentsMetadata();
        for (int i = 0; i < metadata.length; i++) {
            handleTypeDeclaration(bundle, metadata[i]);
        }

        Dictionary[] instances = parser.getInstances();
        for (int i = 0; instances != null && i < instances.length; i++) {
            handleInstanceDeclaration(bundle, instances[i]);
        }
    }

    private void handleInstanceDeclaration(Bundle bundle, Dictionary instance) {

        String component = (String) instance.get("component");
        //String v = (String) instance.get("factory.version");

        DefaultInstanceDeclaration declaration = new DefaultInstanceDeclaration(bundle.getBundleContext(), component, instance);
        declaration.start();

        getComponentsAndInstances(bundle).m_instances.add(declaration);

    }

    /**
     * Adds a component factory to the factory list.
     * @param metadata the new component metadata.
     * @param bundle the bundle.
     */
    private void handleTypeDeclaration(Bundle bundle, Element metadata) {

        DefaultTypeDeclaration declaration = new DefaultTypeDeclaration(bundle.getBundleContext(), metadata);
        declaration.start();

        getComponentsAndInstances(bundle).m_types.add(declaration);

    }


    private ComponentsAndInstances getComponentsAndInstances(Bundle bundle) {
        ComponentsAndInstances cai = m_registry.get(bundle);
        if (cai == null) {
            cai = new ComponentsAndInstances();
            m_registry.put(bundle, cai);
        }
        return cai;
    }


    private static class ComponentsAndInstances {
        List<DefaultTypeDeclaration> m_types = new ArrayList<DefaultTypeDeclaration>();
        List<DefaultInstanceDeclaration> m_instances = new ArrayList<DefaultInstanceDeclaration>();

        void stop() {
            for (DefaultInstanceDeclaration instance : m_instances) {
                instance.stop();
            }
            for (DefaultTypeDeclaration declaration : m_types) {
                declaration.stop();
            }
            m_instances.clear();
            m_types.clear();
        }
    }


}
