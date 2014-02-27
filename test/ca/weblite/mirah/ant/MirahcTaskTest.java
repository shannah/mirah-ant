/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.io.File;
import java.io.FileWriter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shannah
 */
public class MirahcTaskTest {
    
    public MirahcTaskTest() {
    }

    @Test
    public void testExecute() {
        try {
            File destDir = File.createTempFile("dest", "build");
            destDir.delete();
            destDir.mkdir();

            Project p = new Project();
            Path dest = new Path(p, destDir.getAbsolutePath());
            MirahcTask task = new MirahcTask();
            task.setClassPath(Path.systemClasspath);
            task.setDest(dest);
            
            
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
            
            
            
            File srcDir = destDir;
            File pkgDir = new File(srcDir, "mypkg");
            pkgDir.mkdir();
            
            File javaFile = new File(pkgDir, "JavaClass.java");
            File mirahFile = new File(pkgDir, "MirahClass.mirah");
            
            FileWriter fw = new FileWriter(javaFile);
            fw.append(javaSrc);
            fw.close();
            fw = new FileWriter(mirahFile);
            fw.append(mirahSrc);
            fw.close();
            
            task.setSrc(new Path(p, srcDir.getAbsolutePath()));
            
            task.execute();
            
            
        } catch ( Exception ex){
            ex.printStackTrace();
        }
        
    }
    
}
