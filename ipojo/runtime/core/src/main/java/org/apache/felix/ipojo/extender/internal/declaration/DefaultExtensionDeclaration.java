package org.apache.felix.ipojo.extender.internal.declaration;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.extender.ExtensionDeclaration;
import org.apache.felix.ipojo.extender.builder.FactoryBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:28
 * To change this template use File | Settings | File Templates.
 */
public class DefaultExtensionDeclaration extends AbstractDeclaration implements ExtensionDeclaration {

    private final BundleContext m_bundleContext;
    private final FactoryBuilder m_factoryBuilder;
    private final String m_type;
    private ServiceRegistration<?> m_registration;

    public DefaultExtensionDeclaration(BundleContext bundleContext, FactoryBuilder factoryBuilder, String type) {
        m_bundleContext = bundleContext;
        m_factoryBuilder = factoryBuilder;
        m_type = type;
    }

    public FactoryBuilder getFactoryBuilder() {
        return m_factoryBuilder;
    }

    public String getKey() {
        return m_type;
    }

    public void start() {
        m_registration = m_bundleContext.registerService(ExtensionDeclaration.class.getName(), this, getServiceProperties());
        bind();
    }

    private Dictionary<String, ?> getServiceProperties() {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ExtensionDeclaration.ID_PROPERTY, m_type);
        return properties;
    }

    public void stop() {
        if (m_registration != null) {
            m_registration.unregister();
            m_registration = null;
        }
    }

}
