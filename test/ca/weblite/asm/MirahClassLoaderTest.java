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
public class MirahClassLoaderTest {
    
    public MirahClassLoaderTest() {
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
    public void testLoadMirahClass() throws IOException {
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        classLoader.setPath(System.getProperty("sun.boot.class.path"));
        Path cachedir = Files.createTempDirectory("cache");
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        
        MirahClassLoader mirahLoader = new MirahClassLoader(ctx, loader);
        mirahLoader.setPath("test");
        Path mirahCacheDir = Files.createTempDirectory("mirahcache");
        mirahLoader.setCachePath(mirahCacheDir.toString());
        
        ClassNode node = mirahLoader.findClass(
                Type.getObjectType("ca/weblite/asm/SampleMirahClass")
        );
        
        assertTrue("Could not find SampleMirahClass", node!=null);
        
        node = mirahLoader.findClass(
                Type.getObjectType("ca/weblite/asm/SampleMirahClass")
        );
        
        assertTrue("Could not find SampleMirahClass 2nd time", node!=null);
        
    }
    
    @Test
    public void testLoadMirahInterface() throws IOException {
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        classLoader.setPath(System.getProperty("sun.boot.class.path"));
        Path cachedir = Files.createTempDirectory("cache");
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        
        MirahClassLoader mirahLoader = new MirahClassLoader(ctx, loader);
        mirahLoader.setPath("test");
        Path mirahCacheDir = Files.createTempDirectory("mirahcache");
        mirahLoader.setCachePath(mirahCacheDir.toString());
        
        ClassNode node = mirahLoader.findClass(
                Type.getObjectType("ca/weblite/asm/SampleMirahInterface")
        );
        
        assertTrue("Could not find SampleMirahInterface", node!=null);
        
        node = mirahLoader.findClass(
                Type.getObjectType("ca/weblite/asm/SampleMirahInterface")
        );
        
        assertTrue("Could not find SampleMirahInterface 2nd time", node!=null);
        
    }
    
}
