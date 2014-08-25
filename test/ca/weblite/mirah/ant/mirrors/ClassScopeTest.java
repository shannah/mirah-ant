/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ClassScopeTest {
    
    public ClassScopeTest() {
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
    public void testResolveJavaLangObject(){
        PackageScope scope = new PackageScope(null, "ca.weblite.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        scope.setLoader(loader);
        try {
            loader.addImport("java.util.*");
        } catch (IOException ex) {
            Logger.getLogger(ClassScopeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ClassIndex.Node node = loader.find("java.util.Map");
        assertTrue("Cannot find java.util.Map", node!=null);
        ClassScope cs = new ClassScope(scope, node);
        
        String fullName = cs.resolveName("Object", false);
        assertEquals("java.lang.Object not resolved", "java.lang.Object", fullName);
    }
    
    @Test public void testLoadInternalClassWithLocalName(){
        PackageScope scope = new PackageScope(null, "java.util");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        try {
            loader.addImport("java.util.*");
        } catch (IOException ex) {
            Logger.getLogger(ClassScopeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        ClassIndex.Node node = loader.find("java.util.Map");
        assertTrue("Could not find java.util.Map", node != null);
        ClassScope cs = new ClassScope(scope, node);
        String fullName = cs.resolveName("Entry", true);
        assertEquals(
                "Failed to resolve internal class with local type", 
                "Ljava/util/Map$Entry;", 
                fullName
        );
    }
    
    @Test public void testResolveInnerClassOfSuperclass(){
        PackageScope scope = new PackageScope(null, "java.util");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        try {
            loader.fillIndex();
        } catch (IOException ex) {
            Logger.getLogger(ClassScopeTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        ClassIndex.Node node = loader.find(
                "ca.weblite.mirah.ant.mirrors.SampleMapSubclass"
        );
        assertTrue("Could not find SampleMapSubclass", node != null);
        
        ClassScope cs = new ClassScope(scope, node);
        String fullName = cs.resolveName("Entry", true);
        assertEquals(
                "Failed to resolve internal class with local type", 
                "Ljava/util/Map$Entry;", 
                fullName
        );
    }
    
}
