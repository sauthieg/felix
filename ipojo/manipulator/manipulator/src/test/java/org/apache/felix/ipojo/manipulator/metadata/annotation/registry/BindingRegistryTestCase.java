package org.apache.felix.ipojo.manipulator.metadata.annotation.registry;

import junit.framework.TestCase;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.spi.AnnotationVisitorFactory;
import org.apache.felix.ipojo.manipulator.spi.Predicate;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 10/11/12
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class BindingRegistryTestCase extends TestCase {

    private BindingRegistry registry;

    @Mock
    private Reporter reporter;
    @Mock
    private AnnotationVisitorFactory factory;
    @Mock
    private Predicate predicate;

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        registry = new BindingRegistry(reporter);
    }

    public void testBindingAddition() throws Exception {
        registry.addBindings(Collections.singletonList(binding()));

        List<Binding> predicates = registry.getBindings(Type.getType(Provides.class).getDescriptor());

        assertEquals(1, predicates.size());
        Binding found = predicates.get(0);
        assertNotNull(found);
        assertEquals(predicate, found.getPredicate());
        assertEquals(factory, found.getFactory());
    }

    public void testGetBindingsWhenEmpty() throws Exception {
        assertNull(registry.getBindings(Type.getType(Provides.class).getDescriptor()));
        assertNotNull(registry.selection(null));
    }

    private Binding binding() {
        Binding binding = new Binding();
        binding.setAnnotationType(Provides.class);
        binding.setFactory(factory);
        binding.setPredicate(predicate);
        return binding;
    }
}
