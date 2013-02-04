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

package org.apache.felix.ipojo.extender.internal.declaration;

import org.apache.felix.ipojo.extender.Declaration;
import org.apache.felix.ipojo.extender.Status;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 31/01/13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractDeclaration implements Declaration, Status {

    private boolean m_bound = false;
    private String m_message;
    private Throwable m_throwable;

    public boolean isBound() {
        return m_bound;
    }

    public String getMessage() {
        return m_message;
    }

    public Throwable getThrowable() {
        return m_throwable;
    }

    public Status getStatus() {
        return this;
    }

    public void bind() {
        m_bound = true;
        m_message = "Declaration bound";
        m_throwable = null;
    }

    public void unbind(String message) {
        unbind(message, null);
    }

    public void unbind(String message, Throwable throwable) {
        m_bound = false;
        m_message = message;
        m_throwable = throwable;
    }
}
