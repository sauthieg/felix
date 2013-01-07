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
package org.apache.felix.ipojo.bnd;

import aQute.bnd.service.AnalyzerPlugin;
import aQute.bnd.service.Plugin;
import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Resource;
import aQute.libg.reporter.Reporter;
import org.apache.felix.ipojo.manipulator.ManipulationVisitor;
import org.apache.felix.ipojo.manipulator.Pojoization;
import org.apache.felix.ipojo.manipulator.ResourceStore;
import org.apache.felix.ipojo.manipulator.metadata.AnnotationMetadataProvider;
import org.apache.felix.ipojo.manipulator.metadata.CacheableMetadataProvider;
import org.apache.felix.ipojo.manipulator.metadata.CompositeMetadataProvider;
import org.apache.felix.ipojo.manipulator.metadata.FileMetadataProvider;
import org.apache.felix.ipojo.manipulator.visitor.check.CheckFieldConsistencyVisitor;
import org.apache.felix.ipojo.manipulator.visitor.writer.ManipulatedResourcesWriter;
import org.apache.felix.ipojo.metadata.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.felix.ipojo.bnd.Manifests.hasEmbedComponents;

/**
 * A {@code BndIpojoPlugin} is ...
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class PojoizationPlugin implements Plugin, AnalyzerPlugin {

    private static final String PROPERTY_METADATA = "metadata";
    private static final String PROPERTY_USE_LOCAL_SCHEMAS = "use-local-schemas";
    private static final String PROPERTY_EXCLUDE_EMBED_BUNDLES = "exclude-embed-bundles";

    private static final String DEFAULT_METADATA = "META-INF/metadata.xml";
    private static final boolean DEFAULT_USE_LOCAL_SCHEMAS = false;
    private static final boolean DEFAULT_EXCLUDE_EMBED_BUNDLES = false;

    private String m_metadata = DEFAULT_METADATA;
    private boolean m_useLocalSchemas = DEFAULT_USE_LOCAL_SCHEMAS;
    private boolean m_excludeEmbedBundles = DEFAULT_EXCLUDE_EMBED_BUNDLES;

    private Reporter m_reporter;

    public void setProperties(Map<String, String> configuration) {

        // Use metadata file if any
        if (configuration.containsKey(PROPERTY_METADATA)) {
            m_metadata = configuration.get(PROPERTY_METADATA);
        }

        // Use local schemas ?
        if (configuration.containsKey(PROPERTY_USE_LOCAL_SCHEMAS)) {
            m_useLocalSchemas = true;
        }

        // Process embed bundles ?
        if (configuration.containsKey(PROPERTY_EXCLUDE_EMBED_BUNDLES)) {
            m_excludeEmbedBundles = true;
        }
    }

    public void setReporter(Reporter reporter) {
        m_reporter = reporter;
    }

    public boolean analyzeJar(Analyzer analyzer) throws Exception {

        long start = System.currentTimeMillis();

        // Wraps the Bnd Reporter
        BndReporter reporter = new BndReporter(this.m_reporter);

        // Build ResourceStore
        BndJarResourceStore store = new BndJarResourceStore(analyzer, this.m_reporter);
        store.setExcludeEmbedComponents(m_excludeEmbedBundles);

        // Build MetadataProvider
        CompositeMetadataProvider provider = new CompositeMetadataProvider(reporter);

        File file = new File(m_metadata);
        if (file.exists()) {
            // Absolute file system resource
            FileMetadataProvider fmp = new FileMetadataProvider(file, reporter);
            fmp.setValidateUsingLocalSchemas(m_useLocalSchemas);
            provider.addMetadataProvider(fmp);
        } else {
            // In archive resource
            Resource resource = analyzer.getJar().getResource(m_metadata);
            if (resource != null) {
                ResourceMetadataProvider rmp = new ResourceMetadataProvider(resource, reporter);
                rmp.setValidateUsingLocalSchemas(m_useLocalSchemas);
                provider.addMetadataProvider(rmp);
            }
        }
        provider.addMetadataProvider(new AnnotationMetadataProvider(store, reporter));

        CacheableMetadataProvider cache = new CacheableMetadataProvider(provider);
        if (cache.getMetadatas().isEmpty() && hasNoEmbedComponents(analyzer)) {
            return false;
        }

        Pojoization pojoization = new Pojoization(reporter);
        pojoization.disableAnnotationProcessing();
        if (m_useLocalSchemas) {
            pojoization.setUseLocalXSD();
        }

        pojoization.pojoization(store, cache, createVisitor(store, reporter));

        int nbComponents = findElements(cache.getMetadatas(), "component").size();
        int nbHandlers = findElements(cache.getMetadatas(), "handler").size();
        this.m_reporter.progress("iPOJO manipulation performed performed in %s ms (%d components, %d handlers).",
                               (System.currentTimeMillis() - start),
                               nbComponents,
                               nbHandlers);

        // Return true if a new run should be performed after the analyze
        return false;
    }

    private boolean hasNoEmbedComponents(Analyzer analyzer) throws Exception {
        // We don't want to process components from embed jars ?
        return m_excludeEmbedBundles || !hasEmbedComponents(analyzer);

    }

    private List<Element> findElements(List<Element> metadatas, String name) {
        List<Element> found = new ArrayList<Element>();
        for (Element element : metadatas) {
            if (name.equalsIgnoreCase(element.getName())) {
                found.add(element);
            }
        }
        return found;
    }

    private ManipulationVisitor createVisitor(ResourceStore store, BndReporter reporter) {
        ManipulatedResourcesWriter writer = new ManipulatedResourcesWriter();
        writer.setReporter(reporter);
        writer.setResourceStore(store);

        CheckFieldConsistencyVisitor checkFieldConsistencyVisitor = new CheckFieldConsistencyVisitor(writer);
        checkFieldConsistencyVisitor.setReporter(reporter);
        return checkFieldConsistencyVisitor;

    }

}
