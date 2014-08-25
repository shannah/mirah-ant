/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import ca.weblite.mirah.ant.mirrors.ClassIndex.Loader.LoaderType;
import java.io.IOException;

/**
 *
 * @author shannah
 */
public class ResourceLoader {
    ClassIndex.Loader loader;
    String classPath;
    String bootClassPath;
    
    String buildClassPath;
    String javaSourcePath;
    String mirahSourcePath;
    
    
    
    public ResourceLoader(){
        JavaLoader libClassLoader = new JavaLoader(
                LoaderType.LIB_CLASSPATH, 
                null
        );
        JavaLoader buildClassLoader = new JavaLoader(
                LoaderType.BUILD_CLASSPATH, 
                libClassLoader
        );
        JavaLoader javaSourceLoader = new JavaLoader(
                LoaderType.SOURCEPATH, 
                buildClassLoader
        );
        MirahLoader mirahSourceLoader = new MirahLoader(
                LoaderType.SOURCEPATH, 
                javaSourceLoader
        );
        JavaLoader bootLoader = new JavaLoader(
                LoaderType.SOURCEPATH,
                mirahSourceLoader
        );
        
        this.loader = bootLoader;
        
    }
    
    public void setBootClassPath(String cp){
        this.loader.setPath(cp, "application/x-java", LoaderType.BOOT_CLASSPATH);
        this.bootClassPath = cp;
    }
    
    public String getBootClassPath(){
        return this.bootClassPath;
    }
    
    
    public void setBuildClassPath(String cp){
        this.loader.setPath(cp, "application/x-java", LoaderType.BUILD_CLASSPATH);
        this.buildClassPath = cp;
    }
    
    public String getBuildClassPath(){
        return this.buildClassPath;
    }
    
    public void setClassPath(String cp){
        this.loader.setPath(cp, "application/x-java", LoaderType.LIB_CLASSPATH);
        this.classPath = cp;
    }
    
    public String getClassPath(){
        return this.classPath;
    }
    
    public void setJavaSourcePath(String sp){
        this.loader.setPath(sp, "text/x-java", LoaderType.SOURCEPATH);
        this.javaSourcePath = sp;
        
    }
    
    public String getJavaSourcePath(){
        return this.javaSourcePath;
    }
    
    
    public void setMirahSourcePath(String sp){
        this.loader.setPath(sp, "text/x-mirah", LoaderType.SOURCEPATH);
        this.mirahSourcePath = sp;
    }
    
    public String getMirahSourcePath(){
        return this.mirahSourcePath;
                
    }
    
    public ClassIndex.Node find(String path) {
        return this.loader.find(path);
        
    }
    
    public void addImport(String path) throws IOException {
        loader.addImport(path);
    }
    
    public void fillIndex() throws IOException{
        loader.fillIndex();
    }
    
    public void clearCache(String path){
        loader.clearCache(path);
    }
    
    public void clearCache(LoaderType forType){
        loader.clearCache(forType);
    }

    
}
