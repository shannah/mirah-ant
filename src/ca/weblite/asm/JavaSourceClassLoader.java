/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaSourceClassLoader extends BaseClassLoader {
    
    public JavaSourceClassLoader(
            Context context,
            ClassLoader parent,
            ASMClassLoader cacheClassLoader){
        super(context, parent);
        this.cacheClassLoader = cacheClassLoader;
    }
    
    
    private long lastModified;
      
    private class ResourceLoader {
        private String path;
        private File getResource(String file){
            String[] paths = path.split(Pattern.quote(File.pathSeparator));
            for ( String root : paths ){
                File rootFile = new File(root);
                URL url = null;
                if ( rootFile.isDirectory() ){
                    
                    File f = new File(rootFile, file);
                    if ( f.exists()){
                        lastModified = f.lastModified();
                    
                        return f;
                    }
                        
                    
                } 
                
            }
            return null;
        }
    }
    
    
    private ResourceLoader loader;
    private final ASMClassLoader cacheClassLoader;
    private final Map<String,ClassNode> stubCache = new HashMap<>();
    
    @Override
    public ClassNode findStub(Type type)  {
        String classFile = type.getInternalName()+".class";
        ClassNode cached = cacheClassLoader.findClass(type);
        long cacheMtime = cacheClassLoader.getLastModified();
        String javaFile = type.getInternalName().
                replaceAll("\\$","/")
                +".java";
        while (true){
            File file = loader.getResource(javaFile);
            if ( file != null ){
                try {
                    if ( cached != null && cacheMtime >= lastModified){
                        // The cached version is up to date
                        // no need to load the class
                        return cached;
                    }
                    if ( stubCache.containsKey(type.getInternalName())){
                        return stubCache.get(type.getInternalName());
                    }
                    
                    ClassNode stub = new JavaStubFactory(getContext()).
                            createStub(type, file);
                    
                    if ( stub != null ){
                        stubCache.put(type.getInternalName(), stub);
                        return stub;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(JavaSourceClassLoader.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            int lastSlash = javaFile.lastIndexOf("/");
            if ( lastSlash == -1 ){
                break;
            }
            javaFile = javaFile.substring(0, lastSlash)+".java";
            
        }
        
        return super.findStub(type);
    }
    
    
    @Override
    public ClassNode findClass(Type type){
        String classFile = type.getInternalName()+".class";
        ClassNode cached = cacheClassLoader.findClass(type);
        long cacheMtime = cacheClassLoader.getLastModified();
        
        String javaFile = type.getInternalName()+".java";
        while (true){
            File  file = loader.getResource(javaFile);
            if ( file != null ){
                try (FileInputStream bytecode = new FileInputStream(file)){
                    if ( cached != null && cacheMtime >= lastModified){
                        // The cached version is up to date
                        // no need to load the class
                        return cached;
                    }
                    
                    
                    ClassNode node = new ClassNode();
                    ClassReader reader = new ClassReader(bytecode);
                    reader.accept(node, ClassReader.SKIP_CODE);
                    if ( (node.name+".java").equals(javaFile)){
                        return node;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(JavaSourceClassLoader.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
            int lastSlash = javaFile.lastIndexOf("/");
            if ( lastSlash == -1 ){
                break;
            }
            javaFile = javaFile.substring(0, lastSlash)+".java";
        }
        
        return super.findClass(type);
    }
    
    public void setPath(String path){
        if ( loader == null ){
            loader = new ResourceLoader();
            loader.path = path;
        }
    }
    
    public String getPath(){
        return loader.path;
    }
    
    
    
    
}
