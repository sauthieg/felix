package org.apache.felix.ipojo.extender.builder;

import org.apache.felix.ipojo.IPojoFactory;
import org.apache.felix.ipojo.metadata.Element;
import org.osgi.framework.BundleContext;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */
public interface FactoryBuilder {
    IPojoFactory build(BundleContext bundleContext, Element metadata) throws FactoryBuilderException;
}
