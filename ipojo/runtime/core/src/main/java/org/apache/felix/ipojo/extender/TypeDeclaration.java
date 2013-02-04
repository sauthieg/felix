package org.apache.felix.ipojo.extender;

/**
 * Service exposed to instruct a factory creation.
 */
public interface TypeDeclaration extends Declaration {
    /**
     * Gets the component type's name.
     * @return the component type's name.
     */
    String getComponentName();

    /**
     * Gets the component type's version.
     * @return the component type's version
     */
    String getComponentVersion();

    /**
     * Gets the targeted iPOJO Extension (primitive, composite, handler...)
     * @return the targeted extension
     */
    String getExtension();
}
