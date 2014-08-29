/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaSourceClassLoader extends BaseClassLoader {
    private ResourceLoader loader;
    private final ASMClassLoader cacheClassLoader;
    private final Map<String,ClassNode> stubCache = new HashMap<>();
    
    private Set<String> parsedFiles = new HashSet<>();
    
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
                        //System.out.println("Using cached version of "+classFile);
                        return cached;
                    }
                    //System.out.println("Finding stub "+classFile);
                    if ( stubCache.containsKey(type.getInternalName())){
                        //System.out.println("Found in stub cache");
                        return stubCache.get(type.getInternalName());
                    }
                    
                    if ( !parsedFiles.contains(javaFile)){
                        // This java file has already been parsed and it wasn't
                        // in the stub cache
                        parsedFiles.add(javaFile);
                        Set<ClassNode> stubs = 
                                new JavaStubFactory(getContext()).
                                        createStubs(file);
                        
                        for ( ClassNode n : stubs ){
                            stubCache.put(n.name, n);
                        }
                        
                        ClassNode stub = stubCache.get(type.getInternalName());
                        if ( stub != null ){
                            stubCache.put(type.getInternalName(), stub);
                            ClassWriter cw = new ClassWriter(49);
                            String[] ifaces = null;
                            if ( stub.interfaces != null ){
                                int len = stub.interfaces.size();
                                ifaces = new String[len];

                                for ( int i=0; i<len; i++){
                                    ifaces[i] = (String)stub.interfaces.get(i);
                                }
                            }
                            cw.visit(
                                    stub.version,
                                    stub.access,
                                    stub.name,
                                    stub.signature,
                                    stub.superName,
                                    ifaces

                            );
                            String outDirPath = cacheClassLoader.getPath();
                            if ( outDirPath.indexOf(File.pathSeparator) != -1){
                                outDirPath = outDirPath.
                                        split(Pattern.quote(
                                                File.pathSeparator
                                        ))[0];
                            }
                            File outCache = new File(outDirPath, classFile);
                            outCache.getParentFile().mkdirs();
                            try (FileOutputStream fos = 
                                    new FileOutputStream(outCache)){
                                fos.write(cw.toByteArray());
                            } catch ( Exception ex){
                                throw new RuntimeException(ex);
                            }
                            return stub;
                        }
                        
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
