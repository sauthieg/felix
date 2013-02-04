package org.apache.felix.ipojo.extender;

import org.apache.felix.ipojo.extender.builder.FactoryBuilder;

/**
 * iPOJO's extension declaration.
 * This service interface is published to instruct the extender to create a new iPOJO extension (like composite or
 * handler).
 */
public interface ExtensionDeclaration extends Declaration {
    /**
     * The service property specifying the extension name.
     */
    String EXTENSION_NAME_PROPERTY = "ipojo.extension.name";

    /**
     * Gets the factory builder to use to create the factories bound to this extension.
     * @return the factory builder.
     */
    FactoryBuilder getFactoryBuilder();

    /**
     * Gets the extension name. This name must be unique.
     * @return the extension name.
     */
    String getExtensionName();
}
