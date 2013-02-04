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
import java.util.List;

import org.apache.felix.ipojo.extender.internal.BundleProcessor;
import org.osgi.framework.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 31/01/13
 * Time: 16:45
 * To change this template use File | Settings | File Templates.
 */
public class ChainedBundleProcessor implements BundleProcessor {

    private List<BundleProcessor> m_processors = new ArrayList<BundleProcessor>();

    public List<BundleProcessor> getProcessors() {
        return m_processors;
    }

    public void activate(Bundle bundle) {
        for (BundleProcessor processor : m_processors) {
            processor.activate(bundle);
        }
    }

    public void deactivate(Bundle bundle) {
        for (BundleProcessor processor : m_processors) {
            processor.deactivate(bundle);
        }
    }

    public void start() {
        for (BundleProcessor processor : m_processors) {
            processor.start();
        }
    }

    public void stop() {
        // TODO Maybe stop should be in reverse order ?
        for (BundleProcessor processor : m_processors) {
            processor.stop();
        }
    }
}
