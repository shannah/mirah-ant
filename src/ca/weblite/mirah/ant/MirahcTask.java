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
import java.io.File;
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
    private Path bootClassPath;
    private File dest;
    private List<File> files = new ArrayList();
    private String jvmVersion;
    private Path javaSourcesPath;
    private boolean compileJavaSources = false;
    private Javac javac;
    
    public void addConfiguredJavac(final Javac javac) {
        if ( javac != null ){
            compileJavaSources = false;
        }
        this.javac = javac;
    }
    
    private void processJointParameters(){
        if ( javac != null ){
            classPath = javac.getClasspath();
            macroClassPath = javac.getClasspath();
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
        
        MirahCompiler2 c = new MirahCompiler2();
        
        
        if ( getBootClassPath() != null ){
            c.setBootClasspath(getBootClassPath().toString());
        }
        if ( getClassPath() != null ){
            c.setClasspath(getClassPath().toString());
        }
        if ( getMacroClassPath() != null ){
            c.setMacroClasspath(getMacroClassPath().toString());
        }
        if ( getDest() != null ){
            c.setDestination(getDest().toString());
        }
        if ( getJavaSourcesPath() != null ){
            //System.out.println("Java sources path is "+getJavaSourcesPath());
            c.setJavaSourceClasspath(getJavaSourcesPath().toString());
        }
        //System.out.println("Dest dir is "+getDest().toString());
        c.setCompileJavaSources(isCompileJavaSources());
        
        if ( getJvmVersion() != null ){
            c.setJvmVersion(getJvmVersion());
        }
        //System.out.println("Files:");
        for ( File f : javac.getFileList() ){
            //System.out.println("File "+f);
        }
        
        
        
        int res = c.compile(javac.getSrcdir().list());
        //System.out.println("Compile res "+res);
        
        
        javac.execute();
        
            
        
        
        
        
        
        
        
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
    
    
    
}
