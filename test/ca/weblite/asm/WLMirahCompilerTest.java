/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
public class WLMirahCompilerTest {
    
    public WLMirahCompilerTest() {
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
    public void testCompile() throws IOException {
        WLMirahCompiler compiler = new WLMirahCompiler();
        File stubDir = new File("build/test/stubs");
        File cacheDir = new File("build/test/cache");
        File outputDir = new File("build/test/wl_output");
        try {
            if ( stubDir.exists()){
                deleteDir(stubDir);
            }
        } catch (Throwable ex){}
        try {
            if ( cacheDir.exists()){
                deleteDir(cacheDir);
            }
        } catch (Throwable ex){}
        try {
            if ( outputDir.exists()){
                deleteDir(outputDir);
            }
        } catch (Throwable ex){}
        compiler.setJavaStubDirectory(stubDir);
        compiler.setSourcePath("test");
        compiler.setClassCacheDirectory(cacheDir);
        compiler.setDestinationDirectory(outputDir);
        /*
        compiler.compile(new String[]{
            "test/ca/weblite/asm/SampleMirahClass.mirah",
            "test/ca/weblite/asm/testcode/TestMirahClass.mirah"
        });
                */
        
        long start = System.currentTimeMillis();
        compiler.compile(new String[]{"test"});
        System.out.println("First compile took "+(System.currentTimeMillis()-start)+"ms");
        
        compiler = new WLMirahCompiler();
        try {
            if ( stubDir.exists()){
                deleteDir(stubDir);
            }
        } catch (Throwable ex){}
        try {
            if ( cacheDir.exists()){
                deleteDir(cacheDir);
            }
        } catch (Throwable ex){}
        try {
            if ( outputDir.exists()){
                deleteDir(outputDir);
            }
        } catch (Throwable ex){}
        compiler.setJavaStubDirectory(stubDir);
        compiler.setSourcePath("test");
        compiler.setClassCacheDirectory(cacheDir);
        compiler.setDestinationDirectory(outputDir);
        /*
        compiler.compile(new String[]{
            "test/ca/weblite/asm/SampleMirahClass.mirah",
            "test/ca/weblite/asm/testcode/TestMirahClass.mirah"
        });
                */
        
        start = System.currentTimeMillis();
        compiler.compile(new String[]{"test"});
        System.out.println("Second compile took "+(System.currentTimeMillis()-start)+"ms");
        
        compiler = new WLMirahCompiler();
        compiler.setJavaStubDirectory(stubDir);
        compiler.setSourcePath("test");
        compiler.setClassCacheDirectory(cacheDir);
        compiler.setDestinationDirectory(outputDir);
        start = System.currentTimeMillis();
        compiler.compile(new String[]{"test"});
        System.out.println("Third compile took "+(System.currentTimeMillis()-start)+"ms");
        
        start = System.currentTimeMillis();
        compiler.compile(new String[]{"test"});
        System.out.println("Fourth compile took "+(System.currentTimeMillis()-start)+"ms");
        
        compiler.setPrecompileJavaStubs(false);
        start = System.currentTimeMillis();
        compiler.compile(new String[]{"test"});
        System.out.println("Fifth compile (with no stub compilations) took "+(System.currentTimeMillis()-start)+"ms");
        
        
    }
    
    private void deleteDir(File fdir){
    
        Path dir =fdir.toPath();
        try {
          Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

              @Override
              public FileVisitResult visitFile(Path file,
                      BasicFileAttributes attrs) throws IOException {

                  //System.out.println("Deleting file: " + file);
                  Files.delete(file);
                  return CONTINUE;
              }

              @Override
              public FileVisitResult postVisitDirectory(Path dir,
                      IOException exc) throws IOException {

                  //System.out.println("Deleting dir: " + dir);
                  if (exc == null) {
                      Files.delete(dir);
                      return CONTINUE;
                  } else {
                      throw exc;
                  }
              }

          });
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
    
}
