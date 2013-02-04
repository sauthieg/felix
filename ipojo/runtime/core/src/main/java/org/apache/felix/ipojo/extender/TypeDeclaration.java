package org.apache.felix.ipojo.extender;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 30/01/13
 * Time: 17:24
 * To change this template use File | Settings | File Templates.
 */
public interface TypeDeclaration extends Declaration {
    String getComponentName();
    String getComponentVersion();
}
