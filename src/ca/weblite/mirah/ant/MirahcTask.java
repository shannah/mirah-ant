/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import ca.weblite.asm.WLMirahCompiler;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

/**
 * @author shannah
 */
public class MirahcTask extends Task {
    
    private Path classPath;
    private Path macroClassPath;
    private Path macroJarDir;
    private Path bootClassPath;
    
    /**
     * Directory where java source files are precompiled to as stubs.
     */
    private File javaStubDir;
    
    /**
     * Directory where mirah stubs are compiled to.
     */
    private File classCacheDir;
    private File dest;
    private List<File> files = new ArrayList();
    private String jvmVersion;
    private Path javaSourcesPath;
    
    /**
     * Indicates whether the Mirah compiler should run Javac on all
     * java sources that the mirah files depend on.  Inside
     * the ANT task we will usually have this disabled because we
     * will be running the javac task that was provided afterwards.
     */
    private boolean compileJavaSources = false;
    private Javac javac;
    
    /**
     * Whether to run Javac after mirahc is complete.
     */
    private boolean postRunJavac=true;
    
    public void addConfiguredJavac(final Javac javac) {
        if ( javac != null ){
            compileJavaSources = false;
        }
        this.javac = javac;
    }
    
    private void processJointParameters(){
        if ( javac != null ){
            classPath = javac.getClasspath();
            //macroClassPath = javac.getClasspath();
            bootClassPath = javac.getBootclasspath();
            if ( javaSourcesPath == null ){
                
                javaSourcesPath = javac.getSrcdir();
            }
            if ( dest == null ){
                dest = javac.getDestdir();
            }
            jvmVersion = javac.getTarget();
            
        }
    }
    
    
    public @Override
    void execute() throws BuildException {
        processJointParameters();
        
        //MirahCompiler2 c = new MirahCompiler2();
        WLMirahCompiler c = new WLMirahCompiler();
        
        
        if ( getBootClassPath() != null ){
            c.setBootClassPath(getBootClassPath().toString());
        }
        if ( getClassPath() != null ){
            c.setClassPath(getClassPath().toString());
        }
        
        
        // Add any jars in the macrojardir into the macroclasspath
        if ( getMacroJarDir() != null ){
            if (getMacroClassPath() == null){
                macroClassPath = new Path(getProject());
                
            }
            String[] paths = getMacroJarDir().list();
            if ( paths.length > 0 ){
                File jarDir = new File(paths[0]);
                File[] jars = jarDir.listFiles(new FilenameFilter(){

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                    
                });
                for ( File jar : jars ){
                    macroClassPath.add(new Path(getProject(), jar.getAbsolutePath()));
                }
            }
        }
    
        
        if ( getMacroClassPath() != null ){
            c.setMacroClassPath(getMacroClassPath().toString());
        }
        if ( getDest() != null ){
            c.setDestinationDirectory(getDest());
        }
        if ( getJavaSourcesPath() != null ){
            c.setSourcePath(getJavaSourcesPath().toString());
        }
        
        if ( getClassCacheDir() != null ){
            c.setClassCacheDirectory(getClassCacheDir());
        }
        
        if ( getJavaStubDir() != null ){
            c.setJavaStubDirectory(getJavaStubDir());
        }
        //System.out.println("Dest dir is "+getDest().toString());
       // c.setCompileJavaSources(isCompileJavaSources());
        
        if ( getJvmVersion() != null ){
            c.setJvmVersion(getJvmVersion());
        }
        //System.out.println("Files:");
        //for ( File f : javac.getFileList() ){
            //System.out.println("File "+f);
        //}
        try {
            int res = c.compile(javac.getSrcdir().list());
            //System.out.println("Compile res "+res);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
        if ( postRunJavac ){
            javac.execute();
        }
        
            
        
        
        
        
        
        
        
        //stubber.createMirahStubs(src, mirahStub);
    }

    /**
     * @return the classPath
     */
    public Path getClassPath() {
        return classPath;
    }

    /**
     * @param classPath the classPath to set
     */
    public void setClassPath(Path classPath) {
        this.classPath = classPath;
    }

    /**
     * @return the macroClassPath
     */
    public Path getMacroClassPath() {
        return macroClassPath;
    }

    /**
     * @param macroClassPath the macroClassPath to set
     */
    public void setMacroClassPath(Path macroClassPath) {
        this.macroClassPath = macroClassPath;
    }

    /**
     * @return the bootClassPath
     */
    public Path getBootClassPath() {
        return bootClassPath;
    }

    /**
     * @param bootClassPath the bootClassPath to set
     */
    public void setBootClassPath(Path bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    

    /**
     * @return the dest
     */
    public File getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(File dest) {
        this.dest = dest;
    }

    /**
     * @return the jvmVersion
     */
    public String getJvmVersion() {
        return jvmVersion;
    }

    /**
     * @param jvmVersion the jvmVersion to set
     */
    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    /**
     * @return the javaSourcesPath
     */
    public Path getJavaSourcesPath() {
        return javaSourcesPath;
    }

    /**
     * @param javaSourcesPath the javaSourcesPath to set
     */
    public void setJavaSourcesPath(Path javaSourcesPath) {
        this.javaSourcesPath = javaSourcesPath;
    }

    /**
     * @return the compileJavaSources
     */
    public boolean isCompileJavaSources() {
        return compileJavaSources;
    }

    /**
     * @param compileJavaSources the compileJavaSources to set
     */
    public void setCompileJavaSources(boolean compileJavaSources) {
        this.compileJavaSources = compileJavaSources;
    }

    /**
     * @return the postRunJavac
     */
    public boolean isPostRunJavac() {
        return postRunJavac;
    }

    /**
     * @param postRunJavac the postRunJavac to set
     */
    public void setPostRunJavac(boolean postRunJavac) {
        this.postRunJavac = postRunJavac;
    }

    /**
     * @return the macroJarDir
     */
    public Path getMacroJarDir() {
        return macroJarDir;
    }

    /**
     * @param macroJarDir the macroJarDir to set
     */
    public void setMacroJarDir(Path macroJarDir) {
        this.macroJarDir = macroJarDir;
    }

    /**
     * @return the javaStubDir
     */
    public File getJavaStubDir() {
        return javaStubDir;
    }

    /**
     * @param javaStubDir the javaStubDir to set
     */
    public void setJavaStubDir(File javaStubDir) {
        this.javaStubDir = javaStubDir;
    }

    /**
     * @return the classCacheDir
     */
    public File getClassCacheDir() {
        return classCacheDir;
    }

    /**
     * @param classCacheDir the classCacheDir to set
     */
    public void setClassCacheDir(File classCacheDir) {
        this.classCacheDir = classCacheDir;
    }
    
    
    
}
