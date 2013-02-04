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

package org.apache.felix.ipojo.extender.internal.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.extender.builder.FactoryBuilder;
import org.apache.felix.ipojo.extender.builder.FactoryBuilderException;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class ReflectiveFactoryBuilder implements FactoryBuilder {

    private final Constructor<? extends IPojoFactory> m_constructor;

    public ReflectiveFactoryBuilder(Constructor<? extends IPojoFactory> constructor) {
        m_constructor = constructor;
    }

    public IPojoFactory build(BundleContext bundleContext, Element metadata) throws FactoryBuilderException {
        try {
            return m_constructor.newInstance(bundleContext, metadata);
        } catch (InstantiationException e) {
            throw new FactoryBuilderException("Cannot create instance of " + m_constructor.getDeclaringClass(), e);
        } catch (IllegalAccessException e) {
            throw new FactoryBuilderException(m_constructor.getDeclaringClass() + " constructor is not accessible (not public)", e);
        } catch (InvocationTargetException e) {
            throw new FactoryBuilderException("Cannot create instance of " + m_constructor.getDeclaringClass(), e);
        }
    }
}
