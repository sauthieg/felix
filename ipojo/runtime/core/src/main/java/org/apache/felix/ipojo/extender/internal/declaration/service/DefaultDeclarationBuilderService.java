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

package org.apache.felix.ipojo.extender.internal.declaration.service;

import java.util.Hashtable;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.extender.DeclarationBuilder;
import org.apache.felix.ipojo.extender.DeclarationBuilderService;
import org.apache.felix.ipojo.extender.DeclarationHandle;
import org.apache.felix.ipojo.extender.InstanceDeclaration;
import org.apache.felix.ipojo.extender.internal.declaration.DefaultInstanceDeclaration;
import org.osgi.framework.BundleContext;

/**
 * User: guillaume
 * Date: 08/02/2014
 * Time: 21:49
 */
public class DefaultDeclarationBuilderService implements DeclarationBuilderService {

    private final BundleContext context;

    public DefaultDeclarationBuilderService(final BundleContext context) {
        this.context = context;
    }

    public DeclarationBuilder newInstance(final String type) {
        return newInstance(type, null);
    }

    public DeclarationBuilder newInstance(final String type, final String name) {
        return newInstance(type, name, null);
    }

    public DeclarationBuilder newInstance(final String type, final String name, final String version) {
        return new DefaultDeclarationBuilder(context, type)
                .version(version)
                .name(name);
    }

    public void close() {

    }
}
