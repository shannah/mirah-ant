/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shannah
 */
public class PackageScopeTest {
    
    public PackageScopeTest() {
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
    public void testLoadJavaLang(){
        PackageScope scope = new PackageScope(null, "ca.weblite.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("Object", false);
        assertEquals("java.lang.Object not resolved", 
                "java.lang.Object", 
                fullName
        );
    }
    
    @Test
    public void testLoadJavaLangWithParent(){
        Scope parent = new Scope(null);
        
        
        PackageScope scope = new PackageScope(parent, "ca.weblite.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        parent.setLoader(loader);
        
        String fullName = scope.resolveName("Object", false);
        assertEquals(
                "java.lang.Object not resolved", 
                "java.lang.Object", 
                fullName
        );
        
        fullName = scope.resolveName("Object", true);
        assertEquals(
                "java.lang.Object internal name incorrect", 
                "Ljava/lang/Object;", 
                fullName
        );
    }
    
    @Test public void testResolveMirahClass(){
        PackageScope scope = new PackageScope(
                null, 
                "ca.weblite.mirah.ant.mirrors"
        );
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("SampleMirahClass", false);
        assertEquals(
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass not resolved", 
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass", 
                fullName
        );
        
        fullName = scope.resolveName("SampleMirahClass", true);
        assertEquals(
                "ca/weblite/mirah/ant/mirrors/SampleMirahClass internal name"+
                        " incorrect", 
                "Lca/weblite/mirah/ant/mirrors/SampleMirahClass;", 
                fullName
        );
    }
    
    @Test public void testResolveFQN(){
        PackageScope scope = new PackageScope(null, "ca.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("java.util.ArrayList", false);
        assertEquals("java.util.ArrayList not resolved",
                "java.util.ArrayList",
                fullName);
        
        fullName = scope.resolveName("java.util.ArrayList", true);
        assertEquals("java.util.ArrayList internal name not resolved",
                "Ljava/util/ArrayList;",
                fullName);
        
    }
    
    @Test public void testResolveMirahFQN(){
        PackageScope scope = new PackageScope(null, "ca.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName(
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass", 
                false
        );
        assertEquals(
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass not resolved"+
                        " from fqn", 
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass", 
                fullName
        );
        fullName = scope.resolveName(
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass", 
                true
        );
        assertEquals(
                "ca.weblite.mirah.ant.mirrors.SampleMirahClass internal "+
                    "name not resolved",
                "Lca/weblite/mirah/ant/mirrors/SampleMirahClass;",
                fullName);
        
    }
    
    
    @Test public void testResolveArrayType(){
        PackageScope scope = new PackageScope(null, "ca.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("java.lang.Object[]", true);
        assertEquals(
                "Array type resolved wrong", 
                "[Ljava/lang/Object;", 
                fullName
        );
        
        fullName = scope.resolveName("Object[]", true);
        assertEquals(
                "Array type resolved wrong", 
                "[Ljava/lang/Object;", 
                fullName
        );
    }
    
    @Test public void testResolvePrimitiveType(){
        PackageScope scope = new PackageScope(null, "ca.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("int[]", true);
        assertEquals(
                "Array type resolved wrong", 
                "[I", 
                fullName
        );
        
    }
    
    @Test public void testResolveInternalClass(){
        PackageScope scope = new PackageScope(null, "ca.mypkg");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        scope.addImport("java.util.Map.*");
        String fullName = scope.resolveName("Entry", true);
        assertEquals(
                "Failed to resolve internal class with local type", 
                "Ljava/util/Map$Entry;", 
                fullName
        );
    }
    
    @Test public void testResolveLocalNameFromSamePackage(){
        PackageScope scope = new PackageScope(null, "java.util");
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        loader.setMirahSourcePath("test");
        scope.setLoader(loader);
        
        String fullName = scope.resolveName("Map", true);
        assertEquals(
                "Failed to resolve type with local name in same package", 
                "Ljava/util/Map;", 
                fullName
        );
    }
    
}
