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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.extender.internal.BundleProcessor;
import org.apache.felix.ipojo.extender.internal.Extender;
import org.apache.felix.ipojo.extender.internal.builder.ReflectiveFactoryBuilder;
import org.apache.felix.ipojo.extender.internal.declaration.DefaultExtensionDeclaration;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.util.Log;
import org.apache.felix.ipojo.util.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 31/01/13
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class ExtensionBundleProcessor implements BundleProcessor {

    private final Log m_logger;
    private Map<Bundle, List<DefaultExtensionDeclaration>> m_extensions = new HashMap<Bundle, List<DefaultExtensionDeclaration>>();

    public ExtensionBundleProcessor(Log logger) {
        m_logger = logger;
    }

    public void activate(Bundle bundle) {
        Dictionary dict = bundle.getHeaders();
        // Check for abstract factory type
        String extension = (String) dict.get(Extender.IPOJO_EXTENSION);
        if (extension != null) {
            activateExtensions(bundle, extension);
        }
    }

    public void deactivate(Bundle bundle) {
        List<DefaultExtensionDeclaration> declarations = m_extensions.get(bundle);
        if (declarations != null) {
            for (DefaultExtensionDeclaration declaration : declarations) {
                declaration.stop();
            }
            m_extensions.remove(bundle);
        }
    }

    public void start() {
        // Nothing to do
    }

    public void stop() {
        // Construct a new instance to avoid ConcurrentModificationException since deactivate also change the extensions list
        // Sort the list greater first, so last installed bundles are deactivated first
        List<Bundle> bundles = new ArrayList<Bundle>(m_extensions.keySet());
        Collections.sort(bundles, Collections.reverseOrder());
        for (Bundle bundle : bundles) {
            deactivate(bundle);
        }
    }

    /**
     * Parses an IPOJO-Extension manifest header and then creates
     * iPOJO extensions (factory types).
     * @param bundle the bundle containing the header.
     * @param header the header to parse.
     */
    private void activateExtensions(Bundle bundle, String header) {
        String[] extensions = ParseUtils.split(header, ",");
        for (int i = 0; extensions != null && i < extensions.length; i++) {
            String[] segments = ParseUtils.split(extensions[i], ":");

            /*
             * Get the fully qualified type name.
             * type = [namespace] name
             */
            String[] nameparts = ParseUtils.split(segments[0].trim(), " \t");
            String type = nameparts.length == 1 ? nameparts[0] : nameparts[0] + ":" + nameparts[1];

            Class clazz;
            try {
                clazz = bundle.loadClass(segments[1]);
            } catch (ClassNotFoundException e) {
                m_logger.log(Logger.ERROR, "Cannot load the extension " + type, e);
                return;
            }

            try {
                ReflectiveFactoryBuilder builder = new ReflectiveFactoryBuilder(clazz.getConstructor(BundleContext.class, Element.class));
                DefaultExtensionDeclaration declaration = new DefaultExtensionDeclaration(bundle.getBundleContext(), builder, type);

                getBundleDeclarations(bundle).add(declaration);

                declaration.start();

                m_logger.log(Logger.DEBUG, "New factory type available: " + type);
            } catch (NoSuchMethodException e) {
                m_logger.log(Logger.ERROR,
                        String.format("Extension '%s' is missing the required (BundleContext, Element) public constructor", clazz.getName()));
            }
        }
    }

    private List<DefaultExtensionDeclaration> getBundleDeclarations(Bundle bundle) {
        List<DefaultExtensionDeclaration> declarations = m_extensions.get(bundle);
        if (declarations == null) {
            declarations = new ArrayList<DefaultExtensionDeclaration>();
            m_extensions.put(bundle, declarations);
        }
        return declarations;
    }

}
