/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomee.catalina;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface HttpSessionProxy extends HttpSession, Serializable {

    public Object writeReplace() throws ObjectStreamException;

    public static HttpSession get() {
        return (HttpSession) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { HttpSessionProxy.class, HttpSession.class, Serializable.class },
                new Handler());
    }

    public static class Handler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("writeReplace") && method.getParameterTypes().length == 0) {
                return new Serialized();
            }

            try {
                final HttpServletRequest request = OpenEJBSecurityListener.requests.get();
                final HttpSession session = request == null ? null : request.getSession();

                return method.invoke(session, args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }

    public static class Serialized implements Serializable {
        public Object readResolve() throws ObjectStreamException {
            return HttpSessionProxy.get();
        }
    }
}
