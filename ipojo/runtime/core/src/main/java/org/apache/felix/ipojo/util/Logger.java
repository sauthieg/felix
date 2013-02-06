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
package org.apache.felix.ipojo.util;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ErrorHandler;
import org.apache.felix.ipojo.extender.internal.Extender;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * iPOJO Logger.
 * This class is an helper class implementing a simple log system.
 * This logger sends log messages to a log service if available.
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class Logger implements Log {

    /**
     * The iPOJO default log level property.
     */
    public static final String IPOJO_LOG_LEVEL_PROP = "ipojo.log.level";

    /**
     * iPOJO log level manifest header.
     * The uppercase 'I' is important as BND removes all headers that do not
     * start with an uppercase are not added to the bundle.
     * Use an upper case to support bnd.
     */
    public static final String IPOJO_LOG_LEVEL_HEADER = "Ipojo-log-level";

    /**
     * The Log Level ERROR.
     */
    public static final int ERROR = Log.ERROR;

    /**
     * The Log Level WARNING.
     */
    public static final int WARNING = Log.WARNING;

    /**
     * The Log Level INFO.
     */
    public static final int INFO = Log.INFO;

    /**
     * The Log Level DEBUG.
     */
    public static final int DEBUG = Log.DEBUG;

    /**
     * The Bundle Context used to get the
     * log service.
     */
    private BundleContext m_context;

    /**
     * The name of the logger.
     */
    private String m_name;

    /**
     * The instance associated to the logger if any.
     */
    private ComponentInstance m_instance;

    /**
     * The trace level of this logger.
     */
    private int m_level;

    /**
     * Creates a logger.
     * @param context the bundle context
     * @param name the name of the logger
     * @param level the trace level
     */
    public Logger(BundleContext context, String name, int level) {
        m_name = name;
        m_level = level;
        m_context = context;
    }

    /**
     * Creates a logger.
     * @param context the bundle context
     * @param instance the instance
     * @param level the trace level
     */
    public Logger(BundleContext context, ComponentInstance instance, int level) {
        m_instance = instance;
        m_name = m_instance.getInstanceName();
        m_level = level;
        m_context = context;
    }

    /**
     * Create a logger.
     * Uses the default logger level.
     * @param context the bundle context
     * @param name the name of the logger
     */
    public Logger(BundleContext context, String name) {
        this(context, name, getDefaultLevel(context));
    }

    /**
     * Create a logger.
     * Uses the default logger level.
     * @param context the bundle context
     * @param instance the instance
     */
    public Logger(BundleContext context, ComponentInstance instance) {
        this(context, instance, getDefaultLevel(context));
    }

    /**
     * Logs a message.
     * @param level the level of the message
     * @param msg the the message to log
     */
    public void log(int level, String msg) {
        if (m_level >= level) {
            dispatch(level, msg);
        }
        invokeErrorHandler(level, msg, null);
    }

    /**
     * Logs a message with an exception.
     * @param level the level of the message
     * @param msg the message to log
     * @param exception the exception attached to the message
     */
    public void log(int level, String msg, Throwable exception) {
        if (m_level >= level) {
            dispatch(level, msg, exception);
        }
        invokeErrorHandler(level, msg, exception);
    }

    /**
     * Internal log method.
     * @param level the level of the message.
     * @param msg the message to log
     */
    private void dispatch(int level, String msg) {
        LogService log = null;
        ServiceReference ref = null;
        try {
            // Security Check
            if (SecurityHelper.hasPermissionToGetService(LogService.class.getName(), m_context)) {
                ref = m_context.getServiceReference(LogService.class.getName());
            } else {
                Extender.getIPOJOBundleContext().getServiceReference(LogService.class.getName());
            }

            if (ref != null) {
                log = (LogService) m_context.getService(ref);
            }
        } catch (IllegalStateException e) {
            // Handle the case where the iPOJO bundle is stopping
        }

        String message = null;
        String name = m_name;
        if (name == null) {
            name = "";
        }

        switch (level) {
            case DEBUG:
                message = "[DEBUG] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_DEBUG, message);
                } else {
                    System.err.println(message);
                }
                break;
            case ERROR:
                message = "[ERROR] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_ERROR, message);
                } else {
                    System.err.println(message);
                }
                break;
            case INFO:
                message = "[INFO] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_INFO, message);
                } else {
                    System.err.println(message);
                }
                break;
            case WARNING:
                message = "[WARNING] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_WARNING, message);
                } else {
                    System.err.println(message);
                }
                break;
            default:
                message = "[UNKNOWN] " + name + " : " + msg;
                System.err.println(message);
                break;
        }

        if (log != null) {
            m_context.ungetService(ref);
        }
    }

    /**
     * Internal log method.
     * @param level the level of the message.
     * @param msg the message to log
     * @param exception the exception attached to the message
     */
    private void dispatch(int level, String msg, Throwable exception) {
        LogService log = null;
        ServiceReference ref = null;
        try {
            // Security Check
            if (SecurityHelper.hasPermissionToGetService(LogService.class.getName(), m_context)) {
                ref = m_context.getServiceReference(LogService.class.getName());
            } else {
                Extender.getIPOJOBundleContext().getServiceReference(LogService.class.getName());
            }

            if (ref != null) {
                log = (LogService) m_context.getService(ref);
            }
        } catch (IllegalStateException e) {
            // Handle the case where the iPOJO bundle is stopping
        }

        String message = null;
        String name = m_name;
        if (name == null) {
            name = "";
        }

        switch (level) {
            case DEBUG:
                message = "[DEBUG] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_DEBUG, message, exception);
                } else {
                    System.err.println(message);
                    exception.printStackTrace();
                }
                break;
            case ERROR:
                message = "[ERROR] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_ERROR, message, exception);
                } else {
                    System.err.println(message);
                    exception.printStackTrace();
                }
                break;
            case INFO:
                message = "[INFO] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_INFO, message, exception);
                } else {
                    System.err.println(message);
                    exception.printStackTrace();
                }
                break;
            case WARNING:
                message = "[WARNING] " + name + " : " + msg;
                if (log != null) {
                    log.log(LogService.LOG_WARNING, message, exception);
                } else {
                    System.err.println(message);
                    exception.printStackTrace();
                }
                break;
            default:
                message = "[UNKNOWN] " + name + " : " + msg;
                System.err.println(message);
                exception.printStackTrace();
                break;
        }

        if (log != null) {
            m_context.ungetService(ref);
        }
    }

    /**
     * Invokes the error handler service is present.
     * @param level the log level
     * @param msg the message
     * @param error the error
     */
    private void invokeErrorHandler(int level, String msg, Throwable error) {
        // First check the level
        if (level > WARNING) {
            return; // Others levels are not supported.
        }
        // Try to get the error handler service
        try {
            ServiceReference ref = m_context.getServiceReference(ErrorHandler.class.getName());
            if (ref != null) {
                ErrorHandler handler = (ErrorHandler) m_context.getService(ref);
                if (level == ERROR) {
                    handler.onError(m_instance, msg, error);
                } else if (level == WARNING) {
                    handler.onWarning(m_instance, msg, error);
                } // The others case are not supported
                m_context.ungetService(ref);
                return;
            } // Else do nothing...
        } catch (IllegalStateException e) {
            // Ignore
            // The iPOJO bundle is stopping.
        }
    }

    /**
     * Gets the default logger level.
     * The property is searched inside the framework properties,
     * the system properties, and in the manifest from the given
     * bundle context. By default, set the level to {@link Logger#WARNING}.
     * @param context the bundle context.
     * @return the default log level.
     */
    private static int getDefaultLevel(BundleContext context) {
        // First check in the framework and in the system properties
        String level = context.getProperty(IPOJO_LOG_LEVEL_PROP);

        // If null, look in the bundle manifest
        if (level == null) {
            String key = IPOJO_LOG_LEVEL_PROP.replace('.', '-');
            level = (String) context.getBundle().getHeaders().get(key);
        }

        // if still null try the second header
        if (level == null) {
            level = (String) context.getBundle().getHeaders().get(IPOJO_LOG_LEVEL_HEADER);
        }

        if (level != null) {
            if (level.equalsIgnoreCase("info")) {
                return INFO;
            } else if (level.equalsIgnoreCase("debug")) {
                return DEBUG;
            } else if (level.equalsIgnoreCase("warning")) {
                return WARNING;
            } else if (level.equalsIgnoreCase("error")) {
                return ERROR;
            }
        }

        // Either l is null, either the specified log level was unknown
        // Set the default to WARNING
        return WARNING;

    }
}
