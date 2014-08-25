/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class ClassFinderTest {
    
    public ClassFinderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testFindClass() {
        ASMClassLoader loader = new ASMClassLoader(new Context(), null);
        loader.setPath(System.getProperty("sun.boot.class.path"));
        
        ClassFinder finder = new ClassFinder(loader, null);
        
        ClassNode node = finder.findClass("Map");
        assertTrue("Found unimported class", node==null);
        
        finder.addImport("java.util.Map");
        node = finder.findClass("Map");
        assertTrue("Failed to find specifically imported class", node!=null);
        
        node = finder.findClass("Map");
        assertTrue("Failed to find cached class", node!=null);
        
        node = finder.findClass("java.util.Map");
        assertTrue("Failed to find abs classname", node != null);
        
        finder = new ClassFinder(loader, null);
        
        node = finder.findClass("java.util.Map");
        assertTrue(
                "Failed to find abs classname in absense of imports", 
                node!=null
        );
        
        node = finder.findClass("Map");
        assertTrue(
                "Found unimported class after fqn version loaded", 
                node==null
        );
        
        
        node = finder.findClass("java.util.Map.Entry");
        assertTrue(
                "Failed to find abs classname of inner class",
                node!=null
        );
        assertEquals(
                "Inner class internal name incorrect",
                "java/util/Map$Entry",
                node.name
        );
        
        finder.addImport("java.util.*");
        node = finder.findClass("Map");
        assertTrue(
                "Failed to find class with glob import",
                node!=null
        );
        
        finder = new ClassFinder(loader, null);
        finder.addImport("java.util.*");
        node = finder.findClass("Map.Entry");
        assertTrue(
                "Failed to load internal class with glob import",
                node!=null
        );
        
        finder.addImport("java.util.Map.*");
        node = finder.findClass("Entry");
        assertTrue(
                "Failed to load internal class with class glob import",
                node!=null
        );
        
        
        
        
        
        
    }
    
    @Test
    public void testFindStub() {
        ASMClassLoader loader = new ASMClassLoader(new Context(), null);
        loader.setPath(System.getProperty("sun.boot.class.path"));
        
        ClassFinder finder = new ClassFinder(loader, null);
        
        ClassNode node = finder.findStub("Map");
        assertTrue("Found unimported class", node==null);
        
        finder.addImport("java.util.Map");
        node = finder.findStub("Map");
        assertTrue("Failed to find specifically imported class", node!=null);
        
        node = finder.findStub("Map");
        assertTrue("Failed to find cached class", node!=null);
        
        node = finder.findStub("java.util.Map");
        assertTrue("Failed to find abs classname", node != null);
        
        finder = new ClassFinder(loader, null);
        
        node = finder.findStub("java.util.Map");
        assertTrue(
                "Failed to find abs classname in absense of imports", 
                node!=null
        );
        
        node = finder.findStub("Map");
        assertTrue(
                "Found unimported class after fqn version loaded", 
                node==null
        );
        
        
        node = finder.findStub("java.util.Map.Entry");
        assertTrue(
                "Failed to find abs classname of inner class",
                node!=null
        );
        assertEquals(
                "Inner class internal name incorrect",
                "java/util/Map$Entry",
                node.name
        );
        
        finder.addImport("java.util.*");
        node = finder.findStub("Map");
        assertTrue(
                "Failed to find class with glob import",
                node!=null
        );
        
        finder = new ClassFinder(loader, null);
        finder.addImport("java.util.*");
        node = finder.findStub("Map.Entry");
        assertTrue(
                "Failed to load internal class with glob import",
                node!=null
        );
        
        finder.addImport("java.util.Map.*");
        node = finder.findStub("Entry");
        assertTrue(
                "Failed to load internal class with class glob import",
                node!=null
        );
        
        
        finder = new ClassFinder(loader, null);
        finder.addImport("java.util.ArrayList");
        node = finder.findStub("ArrayList");
        assertTrue("Failed to find ArrayList", node!=null);
        
        
        
        
    }
    
}
