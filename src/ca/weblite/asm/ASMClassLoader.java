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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
public class ASMClassLoader extends BaseClassLoader {
    
    
    public ASMClassLoader(Context context, ClassLoader parent){
        super(context, parent);
    }
    
    private long lastModified;

    @Override
    public ClassNode findStub(Type type) {
        return findClass(type);
    }
    
    private class ResourceLoader {
        private String path;
        private InputStream getResourceAsStream(String file){
            String[] paths = path.split(Pattern.quote(File.pathSeparator));
            for ( String root : paths ){
                File rootFile = new File(root);
                if ( rootFile.isDirectory() ){
                    try {
                        File f = new File(rootFile, file);
                        if ( f.exists() ){
                            lastModified = f.lastModified();
                            return new FileInputStream(f);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ASMClassLoader.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                } else if ( rootFile.exists() &&
                        rootFile.getName().endsWith(".jar")){
                    try {
                        JarFile jarFile = new JarFile(rootFile);
                        JarEntry entry = jarFile.getJarEntry(file);
                        
                        if ( entry != null ){
                            lastModified = rootFile.lastModified();
                            return jarFile.getInputStream(entry);
                        }
                        
                        
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(ASMClassLoader.class.getName()).
                                log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ASMClassLoader.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
                
            }
            return null;
        }
    }
    
    
    private ResourceLoader loader;
    
    public ClassNode findClass(Type type) {
        String classFile = type.getInternalName()+".class";
        while (true){
            InputStream bytecode = loader.getResourceAsStream(classFile);
            if ( bytecode != null ){
                try {
                    ClassNode node = new ClassNode();
                    ClassReader reader = new ClassReader(bytecode);
                    reader.accept(node, ClassReader.SKIP_CODE);
                    if ( (node.name+".class").equals(classFile)){
                        return node;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ASMClassLoader.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
            int lastSlash = classFile.lastIndexOf("/");
            if ( lastSlash == -1 ){
                break;
            }
            classFile = classFile.substring(0, lastSlash) +
                    "$" + classFile.substring(lastSlash + 1);
        }
        
        return super.findClass(type);
    }
    
    
    
    public void setPath(String path){
        if ( loader == null ){
            loader = new ResourceLoader();    
        }
        loader.path = path;
    }
    
    public String getPath(){
        return loader.path;
    }
    
    public long getLastModified(){
        return lastModified;
    }
    
}
