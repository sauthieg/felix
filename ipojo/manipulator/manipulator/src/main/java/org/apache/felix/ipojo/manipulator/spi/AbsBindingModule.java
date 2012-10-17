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

package org.apache.felix.ipojo.manipulator.spi;

import org.apache.felix.ipojo.manipulator.metadata.annotation.registry.Binding;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.alwaysTrue;
import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.on;
import static org.apache.felix.ipojo.manipulator.spi.helper.Predicates.or;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/9/12
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbsBindingModule implements Module {

    private List<Binding> bindings = new ArrayList<Binding>();

    public Iterator<Binding> iterator() {
        return bindings.iterator();
    }

    protected AnnotationBindingBuilder bind(Class<? extends Annotation> annotationType) {
        return new AnnotationBindingBuilder(bindings, annotationType);
    }

    public class AnnotationBindingBuilder {
        private Class<? extends Annotation> annotationType;
        private AnnotationVisitorFactory factory;
        private List<Binding> registry;

        public AnnotationBindingBuilder(List<Binding> registry, Class<? extends Annotation> annotationType) {
            this.registry = registry;
            this.annotationType = annotationType;
        }

        public ConditionalBindingBuilder when(Predicate predicate) {
            return new ConditionalBindingBuilder(this, predicate);
        }

        public void to(AnnotationVisitorFactory factory) {
            this.factory = factory;
            registry.add(build());
        }

        private Binding build() {
            Binding binding = new Binding();
            binding.setAnnotationType(annotationType);
            binding.setPredicate(onlySupportedElements(annotationType));
            binding.setFactory(factory);
            return binding;
        }

        private Predicate onlySupportedElements(Class<? extends Annotation> annotationType) {
            Target target = annotationType.getAnnotation(Target.class);
            if (target == null) {
                return alwaysTrue();
            }

            Collection<Predicate> supportedTypes = new HashSet<Predicate>();
            for (ElementType type : target.value()) {
                supportedTypes.add(on(type));
            }

            return or(supportedTypes);
        }
    }

    public class ConditionalBindingBuilder {
        private AnnotationBindingBuilder parent;
        private Predicate predicate;
        private AnnotationVisitorFactory factory;

        public ConditionalBindingBuilder(AnnotationBindingBuilder parent, Predicate predicate) {
            this.parent = parent;
            this.predicate = predicate;
        }

        public AnnotationBindingBuilder to(AnnotationVisitorFactory factory) {
            this.factory = factory;
            bindings.add(build());

            return parent;
        }

        private Binding build() {
            Binding binding = parent.build();
            binding.setPredicate(predicate);
            binding.setFactory(factory);
            return binding;
        }
    }

}
