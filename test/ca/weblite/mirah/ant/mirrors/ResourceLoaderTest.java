/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.IOException;
import java.io.InputStream;
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
public class ResourceLoaderTest {
    
    public ResourceLoaderTest() {
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
    public void testFindSourceFile() throws IOException{
        ResourceLoader loader = new ResourceLoader();
        loader.setJavaSourcePath("test");
        String path = "ca.weblite.mirah.ant.mirrors.SampleJavaClass";
        String internalName = "ca/weblite/mirah/ant/mirrors/SampleJavaClass";
        ClassIndex.Node node = loader.find(
                "ca.weblite.mirah.ant.mirrors.SampleJavaClass"
        );
        
        assertTrue("Sample java class found but shouldn't be", node == null);
        
        loader.addImport("ca.weblite.mirah.ant.mirrors.*");
        
        node = loader.find(
                "ca.weblite.mirah.ant.mirrors.SampleJavaClass"
        );
        
        assertTrue("Sample java class not found", node != null );
        assertEquals(
                "Internal name of SampleJavaClass is incorrect" , 
                internalName, node.internalName
        );
        
        
        node = loader.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass2");
        
        //is = loader.find("ca/weblite/mirah/ant/mirrors/SampleJavaClass2.java");
        assertTrue("Non-existent class found", node == null );
    }
    
    @Test public void testFindClassFile() throws IOException {
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        //System.out.println("Classpath is "+loader.getClassPath());
        ClassIndex.Node node = loader.find("java.lang.Object");
        assertTrue("java.lang.Object found but shouldn't be", node == null);
        
        loader.addImport("java.lang.*");
        node = loader.find("java.lang.Object");
        assertTrue("java.lang.Object not found", node != null);
        assertEquals("java.lang.Object internal name incorrect",
                "java/lang/Object", node.internalName);
    }
    
    @Test public void testFindClassFileMultipath() throws IOException {
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        //System.out.println("Classpath is "+loader.getClassPath());
        ClassIndex.Node node = loader.find("java.lang.Object");
        assertTrue("java.lang.Object found but shouldn't be", node == null);
        
        loader.addImport("java.lang.*");
        node = loader.find("java.lang.Object");
        assertTrue("java.lang.Object not found", node != null);
        assertEquals("java.lang.Object internal name incorrect",
                "java/lang/Object", node.internalName);
    }
    
    @Test public void testFindMirahClass() throws IOException {
        ResourceLoader loader = new ResourceLoader();
        loader.setMirahSourcePath("test");
        String path = "ca.weblite.mirah.ant.mirrors.SampleMirahClass";
        ClassIndex.Node node = loader.find(path);
        assertTrue("SampleMirahClass should not be found before import",
                node == null
        );
        
        loader.addImport("ca.weblite.mirah.ant.mirrors.*");
        node = loader.find(path);
        assertTrue("Sample mirah class should be found but was not",
                node != null);
        assertEquals("SampleMirahClass has incorrect internal name.",
                "ca/weblite/mirah/ant/mirrors/SampleMirahClass",
                node.internalName
        );
    }
    
    @Test public void testFindInternalClass() throws IOException {
        ResourceLoader loader = new ResourceLoader();
        loader.setClassPath(System.getProperty("sun.boot.class.path"));
        loader.setJavaSourcePath("test");
        String path = 
                "ca.weblite.mirah.ant.mirrors.SampleJavaClass."+
                    "StaticInternalClass";
        ClassIndex.Node node = loader.find(path);
        assertTrue("StaticInternalClass should not be found before import",
                node == null
        );
        
        loader.addImport("ca.weblite.mirah.ant.mirrors.*");
        node = loader.find(path);
        assertTrue("Sample internal class should be found but was not",
                node != null);
        assertEquals("Sample internal class has incorrect internal name.",
                "ca/weblite/mirah/ant/mirrors/"+
                        "SampleJavaClass$StaticInternalClass",
                node.internalName
        );
        
        
        loader.addImport("java.util.Map.*");
        path = "java.util.Map.Entry";
        node = loader.find(path);
        assertTrue("Failed to find Map$Entry", node != null);
        assertEquals("Map.Entry has incorrect internal name",
                "java/util/Map$Entry",
                node.internalName
        );
        
        
        
        
        
    }
    
    
}
