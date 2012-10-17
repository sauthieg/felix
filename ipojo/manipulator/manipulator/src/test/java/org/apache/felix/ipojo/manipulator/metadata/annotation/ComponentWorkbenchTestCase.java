package org.apache.felix.ipojo.manipulator.metadata.annotation;

import junit.framework.TestCase;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/12/12
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComponentWorkbenchTestCase extends TestCase {
    public void testBuildWithNoTopLevelElements() throws Exception {

        ComponentWorkbench workbench = new ComponentWorkbench(null, node());
        Element built = workbench.build();
        assertNull(built);

    }

    public void testSimpleBuild() throws Exception {

        Element root = new Element("root", null);

        ComponentWorkbench workbench = new ComponentWorkbench(null, node());
        workbench.setRoot(root);
        Element built = workbench.build();

        assertEquals("root", built.getName());
        assertNull(built.getNameSpace());
        assertEquals(0, built.getAttributes().length);
        assertEquals(0, built.getElements().length);

    }

    public void testElementsAreHierarchicallyPlaced() throws Exception {

        Element root = new Element("root", null);
        Element child = new Element("child", null);

        ComponentWorkbench workbench = new ComponentWorkbench(null, node());
        workbench.setRoot(root);
        workbench.getElements().put(child, null);

        Element built = workbench.build();

        assertEquals("root", built.getName());
        assertNull(built.getNameSpace());
        assertEquals(0, built.getAttributes().length);
        assertEquals(1, built.getElements().length);

        Element builtChild = built.getElements("child")[0];

        assertEquals("child", builtChild.getName());
        assertNull(builtChild.getNameSpace());
        assertEquals(0, builtChild.getAttributes().length);
        assertEquals(0, builtChild.getElements().length);


    }


    private static ClassNode node() {
        ClassNode node = new ClassNode();
        node.name = "my/Component";
        return node;
    }

}
