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

package org.apache.felix.ipojo.manipulator.metadata.annotation.visitor.util;

import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.Type;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/12/12
 * Time: 9:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class Elements {
    /**
     * Build the element object from the given descriptor.
     * @param type : annotation descriptor
     * @return : the element
     */
    public static Element buildElement(Type type) {
        String name = type.getClassName();
        int index = name.lastIndexOf('.');
        String local = name.substring(index + 1);
        String namespace = name.substring(0, index);
        return new Element(local, namespace);
    }

    public static Element getPropertiesElement(ComponentWorkbench workbench) {
        Element properties = workbench.getIds().get("properties");
        if (properties == null) {
            properties = new Element("properties", "");
            workbench.getIds().put("properties", properties);
            workbench.getElements().put(properties, null);
        }
        return properties;
    }
}
