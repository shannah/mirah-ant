/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import ca.weblite.asm.MirahClassIndex.SourceFile;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.Type;

/**
 *
 * @author shannah
 */
public class MirahClassIndexTest {
    
    public MirahClassIndexTest() {
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
    public void testMirahClassIndex() throws IOException {
        MirahClassIndex index = new MirahClassIndex();
        index.setPath("test");
        index.deleteIndex();
        index.indexFile("ca/weblite/asm/SampleMirahClass.mirah");
        SourceFile sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahClass")
        );
        assertTrue(
                "Failed to load source file of SampleMirahClass",
                sourceFile!=null
        );
        assertEquals(
                "Source file name is wrong",
                "SampleMirahClass.mirah",
                sourceFile.file.getName()
        );
        
        //index.save();
        
        index = new MirahClassIndex();
        index.setPath("test");
        sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahClass")
        );
        assertTrue(
                "Shouldn't have found source file before loading index",
                sourceFile==null
        );
        
        index.indexFile("ca/weblite/asm/SampleMirahClass.mirah");
        index.save();
        
        index = new MirahClassIndex();
        index.setPath("test");
        sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahClass")
        );
        assertTrue(
                "Failed to find file that was already indexed",
                sourceFile!=null
        );
        
        assertEquals(
                "Source file name is wrong",
                "SampleMirahClass.mirah",
                sourceFile.file.getName()
        );
        
    }
    
    @Test
    public void testMirahInterfaceIndex() throws IOException {
        MirahClassIndex index = new MirahClassIndex();
        index.setPath("test");
        index.deleteIndex();
        index.indexFile("ca/weblite/asm/SampleMirahInterface.mirah");
        SourceFile sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahInterface")
        );
        assertTrue(
                "Failed to load source file of SampleMirahInterface",
                sourceFile!=null
        );
        assertEquals(
                "Source file name is wrong",
                "SampleMirahInterface.mirah",
                sourceFile.file.getName()
        );
        
        //index.save();
        
        index = new MirahClassIndex();
        index.setPath("test");
        sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahInterface")
        );
        assertTrue(
                "Shouldn't have found source file before loading index",
                sourceFile==null
        );
        
        index.indexFile("ca/weblite/asm/SampleMirahInterface.mirah");
        index.save();
        
        index = new MirahClassIndex();
        index.setPath("test");
        sourceFile = index.findSourceFile(
                Type.getObjectType("ca/weblite/asm/SampleMirahInterface")
        );
        assertTrue(
                "Failed to find file that was already indexed",
                sourceFile!=null
        );
        
        assertEquals(
                "Source file name is wrong",
                "SampleMirahInterface.mirah",
                sourceFile.file.getName()
        );
        
    }
    
}
