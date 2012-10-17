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

package org.apache.felix.ipojo.manipulator.spi.helper;

import org.apache.felix.ipojo.manipulator.spi.BindingContext;
import org.apache.felix.ipojo.manipulator.spi.Predicate;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/10/12
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class Predicates {
    public static Node node() {
        return new Node();
    }

    public static Reference reference(String refId) {
        return new Reference(refId);
    }

    public static Matcher pattern(String regex) {
        return new Matcher(regex);
    }

    public static Predicate on(final ElementType type) {
        return new Predicate() {
            public boolean matches(BindingContext context) {
                return context.getElementType().equals(type);
            }
        };
    }

    public static Predicate alwaysTrue() {
        return new Predicate() {
            public boolean matches(BindingContext context) {
                return true;
            }
        };
    }

    public static Predicate and(final Predicate... predicates) {

        // Optimization
        if (predicates.length == 1) {
            return predicates[0];
        }

        return new Predicate() {
            public boolean matches(BindingContext context) {

                for (Predicate predicate : predicates) {
                    // Quit with first failure
                    if (!predicate.matches(context)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static Predicate or(final Collection<Predicate> predicates) {

        // Optimization
        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        return new Predicate() {
            public boolean matches(BindingContext context) {

                for (Predicate predicate : predicates) {
                    // Quit with first success
                    if (predicate.matches(context)) {
                        return true;
                    }
                }
                // No predicate were matching
                return false;
            }
        };
    }

    public static Predicate or(final Predicate... predicates) {
        return or(Arrays.asList(predicates));
    }

    public static class Reference {

        private String refId;

        public Reference(String refId) {
            this.refId = refId;
        }

        public Predicate exists() {
            return new Predicate() {
                public boolean matches(BindingContext context) {
                    return context.getWorkbench().getIds().containsKey(refId);
                }
            };
        }
    }

    public static class Matcher {

        private Pattern pattern;

        public Matcher(String regex) {
            pattern = Pattern.compile(regex);
        }

        public Predicate matches() {
            return new Predicate() {
                public boolean matches(BindingContext context) {
                    return pattern.matcher(context.getAnnotationType().getClassName()).matches();
                }
            };
        }
    }

    public static class Node {
        public Predicate named(final String expected) {
            return new Predicate() {
                public boolean matches(BindingContext context) {
                    if (context.getNode() instanceof FieldNode) {
                        FieldNode field = (FieldNode) context.getNode();
                        return field.name.equals(expected);
                    }

                    if (context.getNode() instanceof MethodNode) {
                        MethodNode method = (MethodNode) context.getNode();
                        return method.name.equals(expected);
                    }

                    if (context.getNode() instanceof ClassNode) {
                        ClassNode clazz = (ClassNode) context.getNode();
                        return clazz.name.equals(expected);
                    }

                    // Parameters have no name in bytecode

                    return false;
                }
            };
        }
    }

}
