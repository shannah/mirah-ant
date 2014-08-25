/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mirah.tool.Mirahc;

/**
 *
 * @author shannah
 */
public class JavaToMirahTest {
    
    public JavaToMirahTest() {
    }

    //@Test
    public void footestSomeMethod() {
        
        JavaToMirah stubber = new JavaToMirah();
        try {
            stubber.createMirahStubs(new File("/Volumes/Windows VMS/src/codenameone-read-only/CodenameOne/src"), new File("Stubs.mirah"));
            
        } catch (IOException ex) {
            Logger.getLogger(JavaToMirahTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //@Test
    public void testCircularDependency(){
        String javaSrc = "package mypkg;\n"
                +"class JavaClass {\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public static void main(String[] args){\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }"
                +"}";
        
        String mirahSrc = "package mypkg\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"    end\n"
                +" end\n";
        
        runTest(javaSrc, mirahSrc);
    }
    
    
    //@Test
    public void testGenerics(){
        String javaSrc = "package mypkg;\n"
                +"class JavaClass<T,V> {\n"
                +"    private T stored;\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public void setStored(T val){\n"
                +"        stored = val;\n"
                +"    }\n"
                +"    public T getStored(){\n"
                +"        return stored;\n"
                +"    }\n"
                +"    public static void main(String[] args){\n"
                +"        JavaClass jc = new JavaClass<String,String>();\n"
                +"        jc.setStored(\"foobar\");\n"
                +"        System.out.println(\"Stored is \"+jc.getStored());\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }"
                +"}";
        
        String mirahSrc = "package mypkg\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"    end\n"
                +" end\n";
        
        runTest(javaSrc, mirahSrc);
    }
    
    
    //@Test
    public void testStaticField(){
        String javaSrc = "package mypkg;\n"
                +"class JavaClass<T,V> {\n"
                +"    private T stored;\n"
                +"    public static final String STATIC_FIELD = \"STATIC FIELD\";\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public void setStored(T val){\n"
                +"        stored = val;\n"
                +"    }\n"
                +"    public T getStored(){\n"
                +"        return stored;\n"
                +"    }\n"
                +"    public static void main(String[] args){\n"
                +"        JavaClass jc = new JavaClass<String,String>();\n"
                +"        jc.setStored(\"foobar\");\n"
                +"        System.out.println(\"Stored is \"+jc.getStored());\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }"
                +"}";
        
        String mirahSrc = "package mypkg\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"        puts 'In initialize '+JavaClass.STATIC_FIELD;\n"
                +"    end\n"
                +" end\n";
        
        runTest(javaSrc, mirahSrc);
    }
    
    
    
    void runTest(String javaSrc, String mirahSrc){
        
        
    
        JavaToMirah stubber = new JavaToMirah();
        try {
            File temp = File.createTempFile("testCircularDependency", "foo");
            temp.delete();
            temp.mkdir();
            File src = new File(temp, "src");
            src.mkdir();
            File build = new File(temp, "build");
            build.mkdir();
            
            File pkgSrc = new File(src, "mypkg");
            pkgSrc.mkdir();
            
            File javaFile = new File(pkgSrc, "JavaClass.java");
            FileWriter fw = new FileWriter(javaFile);
            fw.append(javaSrc);
            fw.close();
            
            
            File mirahFile = new File(pkgSrc, "MirahClass.mirah");
            fw = new FileWriter(mirahFile);
            fw.append(mirahSrc);
            fw.close();
            
            File mirahStub = new File(pkgSrc, "MirahStubs.mirah");
            
            stubber.createMirahStubs(src, mirahStub);
            
            Mirahc mirahc = new Mirahc();
            mirahc.setClasspath(src.getPath()+":"+build.getPath());
            //mirahc.
            //mirahc.addFileOrDirectory(src);
            mirahc.setDestination(build.getPath());
            mirahc.compile(new String[0]);
            
            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            
            int res = javac.run(null, System.out, System.err, "-cp", src.getPath()+":"+build.getPath(), "-sourcepath", src.getPath(), "-d", build.getPath(), src.getPath()+"/mypkg/JavaClass.java");
            
            assertEquals(0, res);
            
            
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
}
