package org.apache.felix.ipojo.extender;

import java.util.Dictionary;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public interface InstanceDeclaration extends Declaration {
    String COMPONENT_NAME_PROPERTY = "component.name";
    String COMPONENT_VERSION_PROPERTY = "component.version";
    Dictionary<String, Object> getConfiguration();
}
