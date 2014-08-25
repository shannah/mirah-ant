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
public class ClassNameComparatorTest {
    
    public ClassNameComparatorTest() {
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

    /**
     * Test of compare method, of class ClassNameComparator.
     */
    @Test
    public void testCompare() {
        ClassNameComparator cmp = new ClassNameComparator();
        assertTrue("java.lang.Object should match java.lang.*", 
                cmp.compare("java.lang.Object", "java.lang.*")==0);
        
        assertTrue("java.lang.* should match java.lang.Object",
                cmp.compare("java.lang.*", "java.lang.Object")==0);
        
        assertTrue("java.util.* should not match java.lang.Object",
                cmp.compare("java.util.*", "java.lang.Object")!=0);
        
        assertTrue("java.lang.Object should match java.lang.Object",
                cmp.compare("java.lang.Object", "java.lang.Object")==0);
    }
    
}
