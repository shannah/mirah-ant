/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class JavaSourceClassLoaderTest {
    
    public JavaSourceClassLoaderTest() {
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
    public void testLoadJavaSourceClass() throws IOException {
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        classLoader.setPath(System.getProperty("sun.boot.class.path"));
        Path cachedir = Files.createTempDirectory("cache");
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        ClassNode result = loader.findStub(
                Type.getType("ca/weblite/asm/SampleJavaClass")
        );
        assertTrue("Could not find sample java class", result != null);
        assertEquals(
                "SampleJavaClass has wrong name", 
                "ca/weblite/asm/SampleJavaClass",
                result.name
        );
        assertEquals(
                "SampleJavaClass has wrong superclass",
                "java/util/ArrayList",
                result.superName
        );
        
        result = loader.findStub(
                Type.getType(
                        "ca/weblite/asm/SampleJavaClass$StaticInternalClass"
                )
        );
        assertTrue(
                "Could not find sample java class static internal class", 
                result != null
        );
        
        assertEquals(
                "Incorrect name for static internal class",
                "ca/weblite/asm/SampleJavaClass$StaticInternalClass",
                result.name
        );
        
        assertEquals(
                "Incorrect super name for static internal class",
                "java/lang/Object",
                result.superName
        );
        
    }
    
    @Test
    public void testLoadJavaSourceInterface() throws IOException {
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        classLoader.setPath(System.getProperty("sun.boot.class.path"));
        Path cachedir = Files.createTempDirectory("cache");
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        ClassNode result = loader.findStub(
                Type.getType("ca/weblite/asm/JavaInterface")
        );
        assertTrue("Could not find sample java interface", result != null);
        assertEquals(
                "JavaInterface has wrong name", 
                "ca/weblite/asm/JavaInterface",
                result.name
        );
        assertEquals(
                "JavaInterface has wrong superclass",
                "java/lang/Object",
                result.superName
        );
        
        
        
    }
    
}
