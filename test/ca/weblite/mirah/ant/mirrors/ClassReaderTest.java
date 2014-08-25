/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

/**
 *
 * @author shannah
 */
public class ClassReaderTest {
    
    public ClassReaderTest() {
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
    public void testParseClass() throws IOException {
        ClassNode node = new ClassNode();
        ResourceLoader loader = new ResourceLoader();
        
        System.out.println(System.getProperties());
        loader.setClassPath(
                System.getProperty("sun.boot.class.path")+
                        File.pathSeparator+
                        System.getProperty("java.class.path")
        );
        loader.setJavaSourcePath("test");
        File file = new File(
                "test/ca/weblite/mirah/ant/mirrors/SampleJavaClass.java"
        );
        loader.fillIndex();
        assertTrue(
                "java.util.Map not found", 
                loader.find("java.util.Map") != null
        );
        ClassReader reader = new ClassReader(node, file, loader);
        reader.parse();
        
        assertEquals(
                "Class name is incorrect", 
                "ca/weblite/mirah/ant/mirrors/SampleJavaClass", 
                node.name
        );
        
        boolean foundSomeField = false;
        boolean foundObjField = false;
        boolean foundPrivateField = false;
        boolean foundPublicField = false;
        boolean foundProtectedField = false;
        for ( Object o : node.fields ){
            FieldNode n = (FieldNode)o;
            System.out.println("Field "+n.name+" desc "+n.desc);
            if ( "someField".equals(n.name) && "I".equals(n.desc)){
                foundSomeField = true;
            }
            if ("someObjField".equals(n.name) && 
                    "Ljava/lang/Object;".equals(n.desc)){
                foundObjField = true;
                assertTrue(
                        "someObjeField is not public", 
                        ((n.access & Opcodes.ACC_PUBLIC) == 0) 
                );
                assertTrue(
                        "someObjeField is not private", 
                        ((n.access & Opcodes.ACC_PRIVATE) == 0) 
                );
                assertTrue(
                        "someObjeField is not protected", 
                        ((n.access & Opcodes.ACC_PROTECTED) == 0) 
                );
                assertTrue(
                        "someObjeField is not abstract", 
                        ((n.access & Opcodes.ACC_ABSTRACT) == 0) 
                );
                assertTrue(
                        "someObjeField is not static", 
                        ((n.access & Opcodes.ACC_STATIC) == 0) 
                );
            }
            
            if ("privateField".equals(n.name) && 
                    "Ljava/lang/Object;".equals(n.desc)){
                foundPrivateField = true;
                assertTrue(
                        "privateField is not public", 
                        ((n.access & Opcodes.ACC_PUBLIC) == 0) 
                );
                assertTrue(
                        "privateField should be private", 
                        ((n.access & Opcodes.ACC_PRIVATE) != 0) 
                );
                assertTrue(
                        "privateField is not protected", 
                        ((n.access & Opcodes.ACC_PROTECTED) == 0) 
                );
                assertTrue(
                        "privateField is not abstract", 
                        ((n.access & Opcodes.ACC_ABSTRACT) == 0) 
                );
                assertTrue(
                        "privateField is not static", 
                        ((n.access & Opcodes.ACC_STATIC) == 0) 
                );
            }
            
            if ("protectedField".equals(n.name) && 
                    "Ljava/lang/Object;".equals(n.desc)){
                foundProtectedField = true;
                assertTrue(
                        "protectedField should not public", 
                        ((n.access & Opcodes.ACC_PUBLIC) == 0) 
                );
                assertTrue(
                        "protectedField should not be private", 
                        ((n.access & Opcodes.ACC_PRIVATE) == 0) 
                );
                assertTrue(
                        "protectedField should be protected", 
                        ((n.access & Opcodes.ACC_PROTECTED) != 0) 
                );
                assertTrue(
                        "protectedField should not be abstract", 
                        ((n.access & Opcodes.ACC_ABSTRACT) == 0) 
                );
                assertTrue(
                        "protectedField should not static", 
                        ((n.access & Opcodes.ACC_STATIC) == 0) 
                );
            }
            
            if ("publicField".equals(n.name) && 
                    "Ljava/lang/Object;".equals(n.desc)){
                foundPublicField = true;
                assertTrue(
                        "publicField should be public", 
                        ((n.access & Opcodes.ACC_PUBLIC) != 0) 
                );
                assertTrue(
                        "publicField should not be private", 
                        ((n.access & Opcodes.ACC_PRIVATE) == 0) 
                );
                assertTrue(
                        "publicField should not be protected", 
                        ((n.access & Opcodes.ACC_PROTECTED) == 0) 
                );
                assertTrue(
                        "publicField should not be abstract", 
                        ((n.access & Opcodes.ACC_ABSTRACT) == 0) 
                );
                assertTrue(
                        "publicField should not static", 
                        ((n.access & Opcodes.ACC_STATIC) == 0) 
                );
            }
        }
        assertTrue("someField was not found", foundSomeField );
        assertTrue("object field was not found", foundObjField);
        assertTrue("private field was not found", foundPrivateField);
        assertTrue("public field was not found", foundPublicField);
        assertTrue("protected field was not found", foundProtectedField);
    }
    
}
