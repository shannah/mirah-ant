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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class ASMClassLoaderTest {
    
    public ASMClassLoaderTest() {
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

    /**
     * Test of findClass method, of class ASMClassLoader.
     */
    @Test
    public void testFindClass() throws Exception {
        Type type = Type.getObjectType("java/util/Map");
        ASMClassLoader instance = new ASMClassLoader(new Context(), null);
        instance.setPath(System.getProperty("sun.boot.class.path"));
        ClassNode result = instance.findClass(type);
        
        assertTrue("Cannot find java.util.Map", result!=null);
        assertEquals("Incorrect name for Map", "java/util/Map", result.name);
        assertEquals(
                "Incorrect superclass for Map", 
                "java/lang/Object", 
                result.superName
        );
        
    }

    
}
