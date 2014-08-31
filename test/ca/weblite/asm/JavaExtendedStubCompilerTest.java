package ca.weblite.asm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author shannah
 */
public class JavaExtendedStubCompilerTest {
    
    public JavaExtendedStubCompilerTest() {
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

    MethodNode findMethod(ClassNode cls, String name, String descriptor){
        for ( Object o : cls.methods){
            MethodNode method = (MethodNode)o;
            if ( name.equals(method.name) && descriptor.equals(method.desc)){
                return method;
            }
        }
        return null;
    }
    
    MethodNode findMethod(ClassNode cls, String name){
        for ( Object o : cls.methods){
            MethodNode method = (MethodNode)o;
            if ( name.equals(method.name) ){
                return method;
            }
        }
        return null;
    }
    
    @Test
    public void testCompileExtendedJavaStub() throws IOException {
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        classLoader.setPath(System.getProperty("sun.boot.class.path"));
        Path cachedir = Files.createTempDirectory("cache");
        Path mirahCachedir = Files.createTempDirectory("mirahcache");
        
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        
        
        MirahClassLoader mirahLoader = new MirahClassLoader(ctx, loader);
        mirahLoader.setPath("test");
        mirahLoader.setCachePath(mirahCachedir.toFile().getPath());
        mirahLoader.getIndex().updateIndex();
        
        
        JavaExtendedStubCompiler compiler = new JavaExtendedStubCompiler(ctx);
        byte[] result = compiler.compile(
                Type.getObjectType("ca/weblite/asm/SampleJavaClass"),
                new File("test/ca/weblite/asm/SampleJavaClass.java")
        );
        assertTrue(
                "Compiler result should not be null",
                result!=null
        );
        
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(result);
        reader.accept(node, ClassReader.SKIP_CODE);
        
        assertEquals(
                "Resulting class has wrong name",
                "ca/weblite/asm/SampleJavaClass",
                node.name
        );
        
        assertEquals(
                "Resulting class has wrong superclass",
                "java/util/ArrayList",
                node.superName
        );
        
        MethodNode someMethod = findMethod(node, "someMethod");
        assertTrue(
                "someMethod not found in sample class",
                someMethod!=null
        );
        
        assertEquals(
                "someMethod has wrong descriptor",
                "()V",
                someMethod.desc
        );
        
        someMethod = findMethod(node, "someMethodWithParams");
        assertTrue(
                "someMethodWithParams not found in sample class",
                someMethod!=null
        );
        
        assertEquals(
                "someMethodWithParams has wrong descriptor",
                "(ILjava/util/ArrayList;)V",
                someMethod.desc
        );
        
        someMethod = findMethod(node, "someMethodReferencingInternalClass");
        assertTrue(
                "someMethodReferencingInternalClass not found in sample class",
                someMethod!=null
        );
        
        assertEquals(
                "someMethodReferencingInternalClass has wrong descriptor",
                "(Lca/weblite/asm/SampleJavaClass$StaticInternalClass;)V",
                someMethod.desc
        );
        
        
        result = compiler.compile(
                Type.getObjectType("ca/weblite/asm/SampleJavaClassExtendsGeneric"),
                new File("test/ca/weblite/asm/SampleJavaClassExtendsGeneric.java")
        );
            
        assertTrue(
                "Compiler result should not be null",
                result!=null
        );
        
        node = new ClassNode();
        reader = new ClassReader(result);
        reader.accept(node, ClassReader.SKIP_CODE);
        
        assertEquals(
                "Signature is incorrect",
                "Ljava/util/ArrayList<Lca/weblite/asm/SampleJavaClass;>;",
                node.signature
        );
        
        ClassNode arrayList = classLoader.findStub(Type.getObjectType("java/util/ArrayList"));
        System.out.println("Arraylist signature is "+arrayList.signature);
        
        ClassNode attributeList = classLoader.findStub(Type.getObjectType("javax/management/AttributeList"));
        System.out.println("AttributeList signature is "+attributeList.signature);
    }
    
    @Test 
    public void compileDirectory() throws IOException{
        Context ctx = new Context();
        ASMClassLoader classLoader = new ASMClassLoader(ctx, null);
        System.out.println(System.getProperties());
        classLoader.setPath(
                System.getProperty("sun.boot.class.path") +
                        File.pathSeparator +
                        System.getProperty("java.class.path")
        );
        Path cachedir = Files.createTempDirectory("cache");
        Path mirahCachedir = Files.createTempDirectory("mirahcache");
        
        
        ASMClassLoader cacheLoader = new ASMClassLoader(new Context(), null);
        cacheLoader.setPath(cachedir.toFile().getPath());
        
        
        JavaSourceClassLoader loader = 
                new JavaSourceClassLoader(ctx, classLoader, cacheLoader);
        
        loader.setPath("test");
        
        
        MirahClassLoader mirahLoader = new MirahClassLoader(ctx, loader);
        mirahLoader.setPath("test");
        mirahLoader.setCachePath(mirahCachedir.toFile().getPath());
        mirahLoader.getIndex().updateIndex();
        
        
        JavaExtendedStubCompiler compiler = new JavaExtendedStubCompiler(ctx);
    
        File testOut = new File("test_stubs");
        testOut.mkdirs();
        System.out.println("About to compile directory ");
        compiler.compileDirectory(new File("test"), new File("test"), testOut, true);
        System.out.println("Directory compiled");
    }
    
}
