package org.apache.felix.ipojo.extender;

import java.util.Dictionary;

/**
 * Service published to instruct an instance creation.
 */
public interface InstanceDeclaration extends Declaration {
    /**
     * Service property specifying the component type's name.
     */
    String COMPONENT_NAME_PROPERTY = "ipojo.component.name";

    /**
     * Service property specifying the component type's version.
     */
    String COMPONENT_VERSION_PROPERTY = "ipojo.component.version";

    /**
     * Service property specifying the instance name.
     */
    String INSTANCE_NAME = "ipojo.instance.name";

    /**
     * Value used when an instance configuration does not declare its name.
     */
    String UNNAMED_INSTANCE = "unnamed";

    /**
     * The instance configuration.
     * @return the instance configuration
     */
    Dictionary<String, Object> getConfiguration();

    /**
     * @return the component type's name.
     */
    String getComponentName();

    /**
     * @return the component type's version, <code>null</code> if not set.
     */
    String getComponentVersion();

    /**
     * Gets the instance name.
     * @return the instance name, <code>unnamed</code> if not specified.
     */
    String getInstanceName();
}
