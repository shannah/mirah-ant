/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
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
public class ClassIndexTest {
    
    public ClassIndexTest() {
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
    public void testAddClass(){
        ClassIndex index = new ClassIndex();
        ClassIndex.Node cls = index.addClass("ca.weblite.pkg.MyClass", "ca/weblite/pkg/MyClass");
        assertEquals("Simple name is incorrect", "MyClass", cls.simpleName);
        assertEquals("Internan name is incorrect", "ca/weblite/pkg/MyClass", cls.internalName);
        assertEquals("Wrong node type for class", ClassIndex.NodeType.CLASS, cls.type);
    }
    
    
    @Test public void testAddPackage(){
        ClassIndex index = new ClassIndex();
        ClassIndex.Node pkg = index.addPackage("ca.weblite.pkg");
        assertEquals("Smple package name is incorrect", "pkg", pkg.simpleName);
        assertEquals("Internal package name is incorrect", "ca/weblite/pkg", pkg.internalName);
        assertEquals("Wrong node type for package", ClassIndex.NodeType.PACKAGE, pkg.type);
    }
    
    @Test public void testAddExistingPackage(){
        ClassIndex index = new ClassIndex();
        ClassIndex.Node pkg = index.addPackage("ca.weblite.pkg");
        ClassIndex.Node pkg2 = index.addPackage("ca.weblite.pkg");
        assertTrue("Package inserted twice should be same", pkg==pkg2);
        assertEquals("Smple package name is incorrect", "pkg", pkg2.simpleName);
        assertEquals("Internal package name is incorrect", "ca/weblite/pkg", pkg2.internalName);
        assertEquals("Wrong node type for package", ClassIndex.NodeType.PACKAGE, pkg2.type);
    }
    
    @Test public void testAddPackageAtExistingClassPath(){
        ClassIndex index = new ClassIndex();
        ClassIndex.Node cls = index.addClass("ca.weblite.MyClass", "ca/weblite/MyClass");
        ClassIndex.Node pkg = index.addPackage("ca.weblite.MyClass");
        assertTrue("Package added at path of existing class should just return class", cls==pkg);
        assertEquals("Package added at path of existing class should be type class", ClassIndex.NodeType.CLASS, pkg.type);
    }
    
    
}
