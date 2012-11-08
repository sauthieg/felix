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

package org.apache.felix.ipojo.transformation.internal;

import junit.framework.TestCase;
import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.metadata.Element;

public class AutoAttachedHandlerTransformerTestCase extends TestCase {

    @Override
    public void tearDown() throws Exception {
        // Always erase the system property
        // TODO Use System.clearProperty() when switching to Java 5
        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

    public void testOnlyPrimitiveComponentsAreUpdated() throws Exception {

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "test:log");

        AutoAttachedHandlerTransformer transformer = new AutoAttachedHandlerTransformer();

        Element primitiveType = new Element("component", null);
        Element otherComponentType = new Element("component", "in.another.namespace");
        Element otherType = new Element("type", "in.another.namespace");

        transformer.transform(primitiveType);
        assertEquals(1, primitiveType.getElements().length);

        transformer.transform(otherComponentType);
        assertEquals(0, otherComponentType.getElements().length);

        transformer.transform(otherType);
        assertEquals(0, otherType.getElements().length);

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

    public void testCreatedElementWithNamespace() throws Exception {

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "test:log");

        AutoAttachedHandlerTransformer transformer = new AutoAttachedHandlerTransformer();

        Element primitiveType = new Element("component", null);

        transformer.transform(primitiveType);
        assertEquals(1, primitiveType.getElements().length);
        Element child = primitiveType.getElements("log", "test")[0];
        assertNotNull(child);

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

    public void testCreatedElementWithoutNamespace() throws Exception {

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "log");

        AutoAttachedHandlerTransformer transformer = new AutoAttachedHandlerTransformer();

        Element primitiveType = new Element("component", null);

        transformer.transform(primitiveType);
        assertEquals(1, primitiveType.getElements().length);
        Element child = primitiveType.getElements("log")[0];
        assertNotNull(child);

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

    public void testMultipleHandlersDeclaration() throws Exception {

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "test:log, other:test, local");

        AutoAttachedHandlerTransformer transformer = new AutoAttachedHandlerTransformer();

        Element primitiveType = new Element("component", null);

        transformer.transform(primitiveType);
        assertEquals(3, primitiveType.getElements().length);
        assertNotNull(primitiveType.getElements("log", "test")[0]);
        assertNotNull(primitiveType.getElements("test", "other")[0]);
        assertNotNull(primitiveType.getElements("local")[0]);

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

    public void testAutoFixingLeadingAndTrailingSpacesPlusMisplacedCommas() throws Exception {

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, " , test:log,, local , ");

        AutoAttachedHandlerTransformer transformer = new AutoAttachedHandlerTransformer();

        Element primitiveType = new Element("component", null);

        transformer.transform(primitiveType);
        assertEquals(2, primitiveType.getElements().length);
        assertNotNull(primitiveType.getElements("log", "test")[0]);
        assertNotNull(primitiveType.getElements("local")[0]);

        System.setProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE, "");
    }

}
