/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.File;
import java.io.IOException;
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
public class JavaStubFactoryTest {
    
    public JavaStubFactoryTest() {
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
    public void testCreateStub() throws IOException {
        ASMClassLoader loader = new ASMClassLoader(new Context(), null);
        loader.setPath(System.getProperty("sun.boot.class.path"));
        
        JavaStubFactory factory = new JavaStubFactory(loader.getContext());
        Type type = Type.getObjectType("ca/weblite/asm/SampleJavaClass");
        ClassNode stub = factory.createStub(
                type, 
                new File("test/ca/weblite/asm/SampleJavaClass.java")
        );
        assertTrue("Stub is null", stub!=null);
        assertEquals(
                "SampleJavaClass has wrong name",
                "ca/weblite/asm/SampleJavaClass",
                stub.name
        );
        assertEquals(
                "SampleJavaClass has wrong superclass",
                "java/util/ArrayList",
                stub.superName
        );
        
        stub = factory.createStub(
                Type.getObjectType(
                        "ca/weblite/asm/SampleJavaClass$StaticInternalClass"
                ), 
                new File("test/ca/weblite/asm/SampleJavaClass.java")
        );
        assertTrue("Stub is null", stub!=null);
        assertEquals(
                "SampleJavaClass$StaticInternalClass has wrong name",
                "ca/weblite/asm/SampleJavaClass$StaticInternalClass",
                stub.name
        );
        assertEquals(
                "SampleJavaClass$StaticInternalClass has wrong super name",
                "java/lang/Object",
                stub.superName
        );
        
        
        
        
    }
    
}
