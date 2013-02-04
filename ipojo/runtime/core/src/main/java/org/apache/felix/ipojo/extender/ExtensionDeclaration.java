package org.apache.felix.ipojo.extender;

import org.apache.felix.ipojo.extender.builder.FactoryBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public interface ExtensionDeclaration extends Declaration {
    String ID_PROPERTY = "extension.id";
    FactoryBuilder getFactoryBuilder();
}
