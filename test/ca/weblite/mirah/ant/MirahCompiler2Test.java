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

    //@Test
    public void testCompile() {
        
        String mirahSrc = "package mypkg\n"
                +" import java.util.List\n"
                +" import java.awt.*\n"
                +" class MirahClass < JavaClass \n"
                +"    def initialize(jc:JavaClass)\n"
                +"        @jc = jc\n"
                +"        myval = JavaClass.STATIC_FLD\n"
                +"        myval += 10\n"
                +"        list = @jc.getList\n"
                +"        puts list\n"
                +"        puts myval\n"
                +"        jc2 = JavaClass.new\n"
                +"        puts jc2.getString\n"
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
                +"    public String getString(){ return \"foo\";}\n"
                +"}";
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.addFakeFile("MirahClass.mirah", mirahSrc);
        compiler.addFakeJavaSourceFile("mypkg/JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
    //@Test
    public void testCompileInterface() {
        
        String mirahSrc = "package mypkg\n"
                +" import mypkg.JavaClass.*\n"
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
        compiler.addFakeJavaSourceFile("mypkg/JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
    //@Test
    public void testCompileEnum() {
        String mirahSrc = "package ca.weblite.scriblets\n"
                +" import ca.weblite.scriblets.models.JavaClass3\n"
                +" import ca.weblite.scriblets.models.JavaClass3.*\n"
                +" import ca.weblite.scriblets.models.JavaClass2\n"
                +" class MirahClass  \n"
                +"    def initialize(jc:JavaClass3)\n"
                +"        @jc = jc\n"
                +"        myval = JavaClass3.STATIC_FLD\n"
                +"        myval += 10\n"
                +"        list = @jc.getList\n"
                +"        puts list\n"
                +"        puts myval\n"
                +"        jc2 = JavaClass3.new\n"
                +"        puts Direction.NORTH\n"
                +"    end\n"
                +" end\n"
                +" class MDoable < SimpleDoable \n"
                +"    def doNow\n"
                +"        puts 'doing now in mirah'\n"
                +"        jc2 = JavaClass2.new\n"
                + "       dir = jc2.getDirection\n"
                + "       if dir == Direction.NORTH\n"
                + "          puts 'north'\n"
                + "       end\n"
                + "       puts dir\n"
                +"    end\n"
                +" end\n"
                +" class MDoable2 \n"
                +"    implements Doable\n"
                +"    def doNow\n"
                +"        puts 'doing now in mirah'\n"
                +"    end\n"
                +" end\n";
        String javaSrc = "package ca.weblite.scriblets.models;\n"
                +"public class JavaClass3 {\n"
                +"    public static enum Direction {NORTH, SOUTH;\n"+
                "         public static final Direction[] ALL = new Direction[]{NORTH, SOUTH};\n"
                        +"public Direction orthogonal(){\n" +
                        "            switch (this){\n" +
                        "                case SOUTH: return NORTH;\n" +
                        "                case NORTH: return SOUTH;\n" +
                        "            }\n" +
                        "            return null;\n" +
                        "        }\n"
                +     "}\n"
                + "public class Tile {\n" +
                "        private final int row;\n" +
                "        private final int col;\n" +
                "        private Card card;\n" +
                "        \n" +
                "        public Tile(int row, int col){\n" +
                "            this.row = row;\n" +
                "            this.col = col;\n" +
                "        }\n" +
                "\n" +
                "        /**\n" +
                "         * @return the row\n" +
                "         */\n" +
                "        public int getRow() {\n" +
                "            return row;\n" +
                "        }\n"
                +"    }\n"
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
        String javaSrc2 = "package ca.weblite.scriblets.models;\n"
                +"import ca.weblite.scriblets.models.JavaClass3.Direction;"
                +"public class JavaClass2 {\n"
                +"   public Direction getDirection(){ return Direction.NORTH;}"
                +"}";
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.addFakeFile("MirahClass.mirah", mirahSrc);
        compiler.addFakeJavaSourceFile("ca/weblite/scriblets/models/JavaClass3.java", javaSrc);
        compiler.addFakeJavaSourceFile("ca/weblite/scriblets/models/JavaClass2.java", javaSrc2);
        compiler.compile(new String[0]);
    }
    
    
    //@Test
    public void testRealFiles(){
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.setJavaSourceClasspath("./test_resources/src");
        
        String src = "package mypkg\n"
                +"import ca.weblite.scriblets.models.Move\n"
                +"import ca.weblite.scriblets.models.Board\n"
                + "import ca.weblite.scriblets.models.Board.*\n"
                
                + "class MyClass\n"
                + " def hello(move:Move, board:Board)\n"
                + "     dir = Direction.HORIZONTAL\n"
                + "     board.getTile(5,5)\n"
                +"      tiles = move.getTilesUsed\n"
                + "     #puts dir\n"
                + " end\n"
                + "end\n";
        compiler.addFakeFile("MyClass.mirah", src);
        compiler.compile(new String[0]);
    }
    
    
    //@Test
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
        compiler.addFakeJavaSourceFile("mypkg/JavaClass.java", javaSrc);
        compiler.compile(new String[0]);
    }
    
    
    //@Test
    public void testSample1(){
        
        MirahCompiler2 compiler = new MirahCompiler2();
        compiler.setCompileJavaSources(true);
        compiler.setClasspath("sample_src_dirs/sample1/src:sample_src_dirs/sample1:build");
        compiler.setJavaSourceClasspath("sample_src_dirs/sample1/src");
        compiler.setDestination("sample_src_dirs/sample1/build");
        compiler.compile(new String[]{"sample_src_dirs/sample1/src/mypkg/MirahClass.mirah"});
    }

}
