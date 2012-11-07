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

package org.apache.felix.ipojo.manipulator.metadata.annotation;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.registry.BindingRegistry;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class MethodMetadataCollector extends EmptyVisitor implements MethodVisitor {

    /**
     * Binding's registry.
     */
    private BindingRegistry registry;

    /**
     * Output informations.
     */
    private Reporter reporter;

    /**
     * The workbench currently in use.
     */
    private ComponentWorkbench workbench;

    /**
     * Visited field.
     */
    private MethodNode node;

    public MethodMetadataCollector(ComponentWorkbench workbench, MethodNode node, Reporter reporter) {
        this.workbench = workbench;
        this.reporter = reporter;
        this.node = node;
        this.registry = workbench.getBindingRegistry();
    }

    /**
     * Visit method annotations.
     *
     * @param desc : annotation name.
     * @param visible : is the annotation visible at runtime.
     * @return the visitor paring the visited annotation.
     * @see org.objectweb.asm.commons.EmptyVisitor#visitAnnotation(java.lang.String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

        // Return the visitor to be executed (may be null)
        return registry.selection(workbench)
                .method(node)
                .annotatedWith(desc)
                .get();

    }

    /**
     * Visit a parameter annotation.
     *
     * @see org.objectweb.asm.commons.EmptyVisitor#visitParameterAnnotation(int, java.lang.String, boolean)
     */
    public AnnotationVisitor visitParameterAnnotation(int index,
                                                      String desc,
                                                      boolean visible) {
        // Only process annotations on constructor
        if (node.name.equals("<init>")) {

            // Return the visitor to be executed (may be null)
            return registry.selection(workbench)
                    .parameter(node, index)
                    .annotatedWith(desc)
                    .get();

        }
        return super.visitParameterAnnotation(index, desc, visible);
    }



}
