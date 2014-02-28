/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shannah
 */
public class MirahCompiler2Test {
    
    public MirahCompiler2Test() {
    }

    @Test
    public void testCompile() {
        
        String mirahSrc = "package mypkg\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"        myval = JavaClass.STATIC_FLD\n"
                +"        myval += 10\n"
                +"        list = @jc.getList\n"
                +"        puts list\n"
                +"        puts myval\n"
                +"        jc2 = JavaClass.new\n"
                +"    end\n"
                +" end\n";
        String javaSrc = "package mypkg;\n"
                +"class JavaClass {\n"
                +"    public static int STATIC_FLD = 10;\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public java.util.List getList(){return new java.util.ArrayList();}\n"
                +"    public static void main(String[] args){\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }"
                +"}";
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.addFakeFile("MirahClass.mirah", mirahSrc);
        compiler.addFakeJavaSourceFile("JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
    @Test
    public void testCompileInterface() {
        
        String mirahSrc = "package mypkg\n"
                +" import static mypkg.JavaClass.*\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"        myval = JavaClass.STATIC_FLD\n"
                +"        myval += 10\n"
                +"        list = @jc.getList\n"
                +"        puts list\n"
                +"        puts myval\n"
                +"        jc2 = JavaClass.new\n"
                +"    end\n"
                +" end\n"
                +" class MDoable < SimpleDoable \n"
                +"    def doNow\n"
                +"        puts 'doing now in mirah'\n"
                +"    end\n"
                +" end\n"
                +" class MDoable2 \n"
                +"    implements Doable\n"
                +"    def doNow\n"
                +"        puts 'doing now in mirah'\n"
                +"    end\n"
                +" end\n";
        String javaSrc = "package mypkg;\n"
                +"class JavaClass {\n"
                +"    public static interface Doable {\n"
                +"        public void doNow();\n"
                +"    }\n"
                +"    public static class SimpleDoable implements Doable {\n"
                +"        public void doNow(){ System.out.println(\"doing now\");}\n"
                +"    }\n"
                +"    public static int STATIC_FLD = 10;\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public java.util.List getList(){return new java.util.ArrayList();}\n"
                +"    public static void main(String[] args){\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }"
                +"}";
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.addFakeFile("MirahClass.mirah", mirahSrc);
        compiler.addFakeJavaSourceFile("JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
    @Test
    public void testArrayParams() {
        
        String mirahSrc = "package mypkg\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"        myval = JavaClass.STATIC_FLD\n"
                +"        myval += 10\n"
                +"        list = @jc.getList\n"
                +"        puts list\n"
                +"        puts myval\n"
                +"        jc2 = JavaClass.new\n"
                +"        nums = int[2]\n"
                +"        nums[0] = 2\n"
                +"        nums[1] = 3\n"
                +"        nums = jc2.getInts(nums)\n"
                +"        foostr = jc2.getString\n"
                +"        foostr = jc2.getStringWithParam('foo')\n"
                +"        puts 'nums '+nums\n"
                +"    end\n"
                +" end\n";
        String javaSrc = "package mypkg;\n"
                +"class JavaClass {\n"
                
                +"    public String getString(){ return \"foo\";}\n"
                +"    //public String getString = \"foo\";\n"
                +"    public static int STATIC_FLD = 10;\n"
                +"    public static MirahClass getMirahClass(){\n"
                +"        return new MirahClass(new JavaClass());\n"
                +"    }\n"
                +"    public java.util.List getList(){return new java.util.ArrayList();}\n"
                +"    public static void main(String[] args){\n"
                +"        System.out.println(\"We are here \"+getMirahClass());\n"
                +"    }\n"
                +"    public String getStringWithParam(String p){ return p;}\n"
                +"    public int[] getInts(int[] inputs){\n"
                +"        return new int[]{1,2};\n"
                +"    }\n"
                +"}";
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.addFakeFile("MirahClass.mirah", mirahSrc);
        compiler.addFakeJavaSourceFile("JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
}
