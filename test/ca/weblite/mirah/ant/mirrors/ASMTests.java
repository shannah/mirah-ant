/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author shannah
 */
public class ASMTests {
    
    public ASMTests() {
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
    public void testGetObjectType(){
        Type map = Type.getObjectType("java/util/Map");
        assertTrue("Failed to find java/util/Map", map != null);
        
        assertEquals(
                "Binary name of Map wrong", 
                "java.util.Map", 
                map.getClassName()
        );
        
        Type entry = Type.getObjectType("java/util/Map$Entry");
        assertTrue("Failed to find java/util/Map$Entry", entry != null );
        
        assertEquals(
                "Binary name of Entry wrong",
                "java.util.Map$Entry2", 
                entry.getClassName()
        );
        
        
    }
    
    
    @Test public void testClassWriter(){
        ClassWriter writer = new ClassWriter(
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
        );
        
        /*writer.visit(
                1,
                Opcodes.ACC_PUBLIC,
                "ca/weblite/test/TestClass",
                null,
                "java/util/ArrayList",
                null
        );
        */
        
        
        
        Type cls = Type.getObjectType("ca/weblite/test/TestClass");
        assertTrue("Failed to retrieve TestClass", cls != null);
        
        assertEquals(
                "Internal name of TestClass is wrong",
                "ca/weblite/test/TestClass",
                cls.getInternalName()
        );
        
        assertEquals(
                "Binary name of TestClass is wrong",
                "ca.weblite.test.TestClass",
                cls.getClassName()
        );
        
        
    }
    
}
