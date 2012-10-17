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

package org.apache.felix.ipojo.manipulator.metadata.annotation.registry;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.generic.FieldGenericVisitor;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.generic.MethodGenericVisitor;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.generic.ParameterGenericVisitor;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.generic.TypeGenericVisitor;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.util.Elements;
import org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.util.Names;
import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.manipulator.spi.Predicate;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MemberNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/10/12
 * Time: 9:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class Selection implements Iterable<AnnotationVisitor> {

    private BindingRegistry registry;
    private ComponentWorkbench workbench;
    private Reporter reporter;
    private MemberNode node;
    private int index = -1;
    private String annotation;
    private ElementType elementType = null;


    public Selection(BindingRegistry registry, ComponentWorkbench workbench, Reporter reporter) {
        this.registry = registry;
        this.workbench = workbench;
        this.reporter = reporter;
    }

    public Selection field(FieldNode node) {
        this.node = node;
        this.elementType = ElementType.FIELD;
        return this;
    }

    public Selection method(MethodNode node) {
        this.node = node;
        this.elementType = ElementType.METHOD;
        return this;
    }

    public Selection type(ClassNode node) {
        this.node = node;
        this.elementType = ElementType.TYPE;
        return this;
    }

    public Selection parameter(MethodNode node, int index) {
        this.index = index;
        this.node = node;
        this.elementType = ElementType.PARAMETER;
        return this;
    }

    public Selection annotatedWith(String desc) {
        this.annotation = desc;
        return this;
    }

    public AnnotationVisitor get() {
        Iterator<AnnotationVisitor> i = iterator();
        if (iterator().hasNext()) {
            return i.next();
        }
        return null;
    }

    public Iterator<AnnotationVisitor> iterator() {

        List<AnnotationVisitor> visitors = new ArrayList<AnnotationVisitor>();

        BindingContext context = new BindingContext(workbench, reporter, Type.getType(annotation), node, elementType, index);
        List<Binding> predicates = registry.getBindings(annotation);

        if (predicates != null && !predicates.isEmpty()) {
            collectMatchingVisitors(predicates, context, visitors);
        }

        if (visitors.isEmpty() && !registry.getDefaultBindings().isEmpty()) {
            collectMatchingVisitors(registry.getDefaultBindings(), context, visitors);
        }


        return visitors.iterator();
    }

    private void collectMatchingVisitors(List<Binding> bindings, BindingContext context, List<AnnotationVisitor> visitors) {
        for (Binding binding : bindings) {
            if (binding.getPredicate().matches(context)) {
                AnnotationVisitor visitor = binding.getFactory().newAnnotationVisitor(context);
                visitors.add(visitor);
            }
        }
    }

}
