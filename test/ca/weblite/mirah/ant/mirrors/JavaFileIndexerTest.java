/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class JavaFileIndexerTest {
    
    public JavaFileIndexerTest() {
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

    @Test public void testIndexFile() throws IOException{
        ClassIndex index = new ClassIndex();
        JavaFileIndexer indexer = new JavaFileIndexer();
        String path = "test/ca/weblite/mirah/ant/mirrors/SampleJavaClass.java";
        try (FileInputStream fis = new FileInputStream(path)){
            indexer.index(path, "test", fis, index, null);
        }
        ClassIndex.Node node = index.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass");
        assertTrue("Node not found for SampleJavaClass", node != null );
        assertEquals("Simple name incorrect for SampleJavaClass", "SampleJavaClass", node.simpleName);
        assertEquals("Internal name incorrect for SampleJavaClass", "ca/weblite/mirah/ant/mirrors/SampleJavaClass", node.internalName);
        assertEquals("Node type for class incorrect", ClassIndex.NodeType.CLASS, node.type);
        
                
    }
    
    @Test public void testIndexInternalClasses() throws IOException{
        ClassIndex index = new ClassIndex();
        JavaFileIndexer indexer = new JavaFileIndexer();
        String path = "test/ca/weblite/mirah/ant/mirrors/SampleJavaClass.java";
        try (FileInputStream fis = new FileInputStream(path)){
            indexer.index(path, "ca/weblite/mirah/ant/mirrors/SampleJavaClass.java", fis, index, null);
        }
        ClassIndex.Node node = index.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass.StaticInternalClass");
        assertTrue("Node not found for StaticInternalClass", node != null );
        assertEquals("Simple name incorrect for StaticInternalClass", "StaticInternalClass", node.simpleName);
        assertEquals("Internal name incorrect for SampleJavaClass", "ca/weblite/mirah/ant/mirrors/SampleJavaClass$StaticInternalClass", node.internalName);
        assertEquals("Node type for internal class incorrect", ClassIndex.NodeType.CLASS, node.type);
        
        node = index.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass.StaticInternalClass.NestedStaticInternalClass");
        assertTrue("Node not found for NestedStaticInternalClass", node != null );
        assertEquals("Simple name incorrect for NestedStaticInternalClass", "NestedStaticInternalClass", node.simpleName);
        assertEquals("Internal name incorrect for NestedStaticInternalClass", "ca/weblite/mirah/ant/mirrors/SampleJavaClass$StaticInternalClass$NestedStaticInternalClass", node.internalName);
        assertEquals("Node type for nested internal class incorrect", ClassIndex.NodeType.CLASS, node.type);
        
        node = index.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass.StaticInternalEnum");
        assertTrue("Node not found for StaticInternalEnum", node != null );
        assertEquals("Simple name incorrect for StaticInternalEnum", "StaticInternalEnum", node.simpleName);
        assertEquals("Internal name incorrect for NestedStaticInternalEnum", "ca/weblite/mirah/ant/mirrors/SampleJavaClass$StaticInternalEnum", node.internalName);
        assertEquals("Node type for nested internal enum incorrect", ClassIndex.NodeType.CLASS, node.type);
        
        node = index.find("ca.weblite.mirah.ant.mirrors.SampleJavaClass.StaticInternalInterface");
        assertTrue("Node not found for StaticInternalInterface", node != null );
        assertEquals("Simple name incorrect for StaticInternalInterface", "StaticInternalInterface", node.simpleName);
        assertEquals("Internal name incorrect for NestedStaticInternalInterface", "ca/weblite/mirah/ant/mirrors/SampleJavaClass$StaticInternalInterface", node.internalName);
        assertEquals("Node type for nested internal interface incorrect", ClassIndex.NodeType.CLASS, node.type);
        
                
    }
    
}
