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

package org.apache.felix.ipojo.extender;

/**
 * This service provides a way for users to manage declarations through code.
 *
 * Notice that produced declarations are immutable once build. But it is possible to re-use a
 * builder to share some configuration through instances.
 *
 * <pre>
 *     // Obtain the service through the service registry
 *     DeclarationBuilderService service = ...
 *
 *     // Get a fresh instance builder
 *     DeclarationBuilder builder = service.newInstance("the.full.name.of.the.component.to.instantiate");
 *
 *     DeclarationHandle handle = builder.name("a-unique-name") // Make sure name is unique for the expected type
 *                                       .configure()
 *                                           .property("a-property", "a-value")
 *                                           .property("another-property", "another-value")
 *                                           .build();
 *
 *     // Publish the instanceDeclaration service
 *     handle.publish();
 * </pre>
 *
 * Except the {@link DeclarationBuilder#build()} call, all methods are optional:
 * <ul>
 *     <li>{@link DeclarationBuilder#name(String)}: if no name is provided,
 *     a default one will be generated by iPOJO.</li>
 *     <li>{@link DeclarationBuilder#version(String)}: if no version is provided,
 *     the first un-versioned type will be used.</li>
 *     <li>{@link DeclarationBuilder#configure()}: if no configuration is required, can be omitted.</li>
 * </ul>
 *
 * Once a handle has been created, its configuration (name, type, version and properties) is immutable. It can
 * only be {@linkplain DeclarationHandle#publish() published} (so that the framework will try to instantiate the
 * instance) or {@linkplain DeclarationHandle#retract() retracted} (framework will remove the instance).
 *
 * Notice that all created instances will appear as "coming from" the bundle that requires the
 * {@link DeclarationBuilderService} service. just like having a {@literal metadata.xml} file in your
 * bundle that declares your instances.
 *
 * @see org.apache.felix.ipojo.extender.InstanceDeclaration
 * @see org.apache.felix.ipojo.extender.DeclarationBuilder
 * @see org.apache.felix.ipojo.extender.ConfigurationBuilder
 * @see org.apache.felix.ipojo.extender.DeclarationHandle
 *
 * @since 1.12
 */
public interface DeclarationBuilderService {
    DeclarationBuilder newInstance(String type);
    DeclarationBuilder newInstance(String type, String name);
    DeclarationBuilder newInstance(String type, String name, String version);
}
