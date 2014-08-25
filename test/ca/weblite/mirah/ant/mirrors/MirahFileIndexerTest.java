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
public class MirahFileIndexerTest {
    
    public MirahFileIndexerTest() {
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

    
    @Test public void testIndexMirahFile() throws  IOException{
        ClassIndex index = new ClassIndex();
        MirahFileIndexer indexer = new MirahFileIndexer();
        
        String path = "test/ca/weblite/mirah/ant/mirrors/SampleMirahClass.mirah";
        String relPath = "ca/weblite/mirah/ant/mirrors/SampleMirahClass.mirah";
        
        assertTrue("Mirah indexer cannot index mirah file but should be able to ", indexer.canIndex(path, relPath, null));
        try (FileInputStream fis = new FileInputStream(path)){
            indexer.index(path, relPath, fis, index, null);
        }
        
        ClassIndex.Node classNode = index.find("ca.weblite.mirah.ant.mirrors.SampleMirahClass");
        assertTrue("SampleMirahClass not found", classNode != null );
        assertEquals("SampleMirahClass has wrong simple name", "SampleMirahClass", classNode.simpleName);
        assertEquals("SampleMirahClass has wrong internal name", "ca/weblite/mirah/ant/mirrors/SampleMirahClass", classNode.internalName);
        assertEquals("SampleMirahClass has wrong node type", ClassIndex.NodeType.CLASS, classNode.type);
        
        classNode = index.find("com.example.anotherpackage.AnotherMirahClass");
        assertTrue("AnotherMirahClass not found", classNode != null );
        assertEquals("AnotherMirahClass has wrong simple name", "AnotherMirahClass", classNode.simpleName);
        assertEquals("AnotherMirahClass has wrong internal name", "com/example/anotherpackage/AnotherMirahClass", classNode.internalName);
        assertEquals("AnotherMirahClass has wrong node type", ClassIndex.NodeType.CLASS, classNode.type);
        
    }
    
}
