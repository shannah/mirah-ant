/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import ca.weblite.asm.ASMClassLoader;
import ca.weblite.asm.ClassFinder;
import ca.weblite.asm.Context;
import java.util.Arrays;
import java.util.Map;
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
public class TypeUtilTest {
    
    public TypeUtilTest() {
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
    public void testSignature(){
        ASMClassLoader loader = new ASMClassLoader(new Context(), null);
        loader.setPath(System.getProperty("sun.boot.class.path"));
        ClassFinder classFinder = new ClassFinder(loader,null);
        
        String type = "Object";
        String sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Object", 
                "Ljava/lang/Object;", 
                sig
        );
        
        classFinder.addImport("java.util.*");
        
        type = "List<Object>";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for List<Object>",
                "Ljava/util/List<Ljava/lang/Object;>;",
                sig
        );
        
        type = "Map<String,List>";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Map<String,List>",
                "Ljava/util/Map<Ljava/lang/String;Ljava/util/List;>;",
                sig
        );
        
        type = "Map<String, List>";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Map<String, List> (with spaces)",
                "Ljava/util/Map<Ljava/lang/String;Ljava/util/List;>;",
                sig
        );
        
        type = "Map<String, List>[]";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Map<String, List>[] (with spaces)",
                "[Ljava/util/Map<Ljava/lang/String;Ljava/util/List;>;",
                sig
        );
        
        type = "Map<String[], List>[]";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Map<String[], List>[] (with spaces)",
                "[Ljava/util/Map<[Ljava/lang/String;Ljava/util/List;>;",
                sig
        );
        
        type = "Map<byte[], List>[]";
        sig = TypeUtil.getTypeSignature(type, classFinder);
        assertEquals(
                "Failed to get signature for Map<byte[], List>[] (with spaces)",
                "[Ljava/util/Map<[BLjava/util/List;>;",
                sig
        );
        
        
        sig = TypeUtil.getMethodSignature(classFinder, "void");
        assertEquals(
                "Failed to get void method signature",
                "()V",
                sig
        );
        sig = TypeUtil.getMethodSignature(classFinder, "List<Object>");
        assertEquals(
                "Failed to get method signature for List<Object>)",
                "()Ljava/util/List<Ljava/lang/Object;>;",
                sig
        );
        
        sig = TypeUtil.getMethodSignature(classFinder, "List<Object>", "List<Object>");
        assertEquals(
                "Failed to get method signature for List<Object>)",
                "(Ljava/util/List<Ljava/lang/Object;>;)Ljava/util/List<Ljava/lang/Object;>;",
                sig
        );
        
        sig = TypeUtil.getClassSignature(
                classFinder, 
                Arrays.asList(new String[]{"T"}),
                "ArrayList<T>",
                "List<T>"
        );
        assertEquals(
                "Failed to get class signature for MyClass<T> extends ArrayList<T>",
                "<T:Ljava/lang/Object;>Ljava/util/ArrayList<TT;>;Ljava/util/List<TT;>;",
                sig
        );
        
        sig = TypeUtil.getClassSignature(
                classFinder, 
                null,
                "ArrayList<Map>",
                "List<Map>"
        );
        assertEquals(
                "Failed to get class signature for MyClass<T> extends ArrayList<T>",
                "Ljava/util/ArrayList<Ljava/util/Map;>;Ljava/util/List<Ljava/util/Map;>;",
                sig
        );
        
        
    }

    /**
     * Test of isPrimitiveType method, of class TypeUtil.
     */
    @Test
    public void testIsPrimitiveType() {
        String type = "java/lang/Object";
        boolean expResult = false;
        boolean result = TypeUtil.isPrimitiveType(type);
        assertEquals("Object is not primitive", expResult, result);
        
        type = "float";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("float should be primitive", expResult, result);
        
        type = "int";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("int should be primitive", expResult, result);
        
        type = "double";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("double should be primitive", expResult, result);
        
        type = "boolean";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("boolean should be primitive", expResult, result);
        
        type = "short";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("short should be primitive", expResult, result);
        
        type = "long";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("long should be primitive", expResult, result);
        
        type = "byte";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("byte should be primitive", expResult, result);
        
        type = "char";
        expResult = true;
        result = TypeUtil.isPrimitiveType(type);
        assertEquals("float should be primitive", expResult, result);
        
    }

    /**
     * Test of getPrimitiveDescriptor method, of class TypeUtil.
     */
    @Test
    public void testGetPrimitiveDescriptor() {
        String type = "int";
        String expResult = "I";
        String result = TypeUtil.getPrimitiveDescriptor(type);
        assertEquals("Incorrect descriptor for int", expResult, result);
        
    }

    /**
     * Test of getDescriptor method, of class TypeUtil.
     */
    @Test
    public void testGetDescriptor() {
        String internalName = "java/lang/Object";
        String expResult = "Ljava/lang/Object;";
        String result = TypeUtil.getDescriptor(internalName);
        assertEquals("Incorrect descriptor for Object", expResult, result);
        
        internalName = "int";
        expResult = "I";
        result = TypeUtil.getDescriptor(internalName);
        assertEquals("Incorrect descriptor for int", expResult, result);
        
        
        
    }

    /**
     * Test of getArrayDescriptor method, of class TypeUtil.
     */
    @Test
    public void testGetArrayDescriptor() {
        String internalName = "java/lang/Object";
        int dimensions = 1;
        String expResult = "[Ljava/lang/Object;";
        String result = TypeUtil.getArrayDescriptor(internalName, dimensions);
        assertEquals("Incorrect array descriptor", expResult, result);
        
        internalName = "java/lang/Object";
        dimensions = 2;
        expResult = "[[Ljava/lang/Object;";
        result = TypeUtil.getArrayDescriptor(internalName, dimensions);
        assertEquals("Incorrect array descriptor for 2-dim", expResult, result);
        
        internalName = "int";
        dimensions = 2;
        expResult = "[[I";
        result = TypeUtil.getArrayDescriptor(internalName, dimensions);
        assertEquals(
                "Incorrect array descriptor for 2-dim int", 
                expResult, 
                result
        );
        
        
    }

    /**
     * Test of isArrayType method, of class TypeUtil.
     */
    @Test
    public void testIsArrayType() {
        String type = "java.lang.Object[]";
        boolean expResult = true;
        boolean result = TypeUtil.isArrayType(type);
        assertEquals("isArrayType false negative", expResult, result);
        
        type = "java.lang.Object";
        expResult = false;
        result = TypeUtil.isArrayType(type);
        assertEquals("isArrayType false positive", expResult, result);
        
        type = "java.lang.Object[][]";
        expResult = true;
        result = TypeUtil.isArrayType(type);
        assertEquals("isArrayType false negative in 2dim", expResult, result);
        
    }

    /**
     * Test of getArrayTypeDimension method, of class TypeUtil.
     */
    @Test
    public void testGetArrayTypeDimension() {
        String type = "java.lang.Object";
        int expResult = 0;
        int result = TypeUtil.getArrayTypeDimension(type);
        assertEquals(
                "getArrayTypeDimension should be 0 for non array type",
                expResult, result
        );
        
        type = "java.lang.Object[]";
        expResult = 1;
        result = TypeUtil.getArrayTypeDimension(type);
        assertEquals(
                "getArrayTypeDimension should be 1 for array type",
                expResult, result
        );
        
        type = "java.lang.Object[][]";
        expResult = 2;
        result = TypeUtil.getArrayTypeDimension(type);
        assertEquals(
                "getArrayTypeDimension should be 2 for 2-dim array type",
                expResult, result
        );
        
        
    }

    /**
     * Test of getArrayElementType method, of class TypeUtil.
     */
    @Test
    public void testGetArrayElementType() {
        String type = "java.lang.Object[]";
        String expResult = "java.lang.Object";
        String result = TypeUtil.getArrayElementType(type);
        assertEquals("getArrayElementType incorrect", expResult, result);
        
        type = "java.lang.Object[][]";
        expResult = "java.lang.Object";
        result = TypeUtil.getArrayElementType(type);
        assertEquals(
                "getArrayElementType incorrect for 2-dim", 
                expResult, 
                result
        );
       
    }
    
}
