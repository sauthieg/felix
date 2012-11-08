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

import org.apache.felix.ipojo.ComponentFactory;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.transformation.ElementTransformer;

/**
 * Read the {@literal org.apache.felix.ipojo.handler.auto.primitive} System property and attach declared
 * handlers to all new primitive ComponentFactories.
 * The value is a String parsed as a list (comma separated, spaces are supported). Each element is
 * the fully qualified name of the handler {@literal namespace:name}.
 *
 * Value example: {@literal ns:local, ns2:log}
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class AutoAttachedHandlerTransformer implements ElementTransformer {
    public void transform(final Element element) {

        if ("component".equals(element.getName()) && (element.getNameSpace() == null)) {
            // This modified only transform primitive component types

            // TODO this parsing of a System Property shouldn't be done only 1 time when it is created ?
            // Manage auto attached handlers.
            String propertyValue = System.getProperty(ComponentFactory.HANDLER_AUTO_PRIMITIVE);
            if (propertyValue != null && propertyValue.length() != 0) {

                // Get handlers names
                String[] handlers = ParseUtils.split(propertyValue, ",");
                for (int i = 0; i < handlers.length; i++) {

                    // Trim value and split to get namespace + name of the handler
                    String handler = handlers[i].trim();
                    String[] segments = ParseUtils.split(handler, ":");

                    Element child = null;
                    switch (segments.length) {
                        case 1:
                            child = new Element(segments[0], null);
                            break;
                        case 2:
                            child = new Element(segments[1], segments[0]);
                            break;
                    }

                    if  (child != null) {
                        // Append the new Handler Element in the component's metadata
                        element.addElement(child);
                    } // else ignore malformed handler name
                }
            }
        }
    }
}
