/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import mirah.impl.MirahParser;
import mirah.lang.ast.ClassDefinition;
import mirah.lang.ast.InterfaceDeclaration;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeScanner;
import mirah.lang.ast.StreamCodeSource;
import org.mirah.mmeta.SyntaxError;
import org.objectweb.asm.Type;

/**
 *
 * @author shannah
 */
public class MirahClassIndex {
    
    
    
    private long lastModified;
    private ResourceLoader loader = new ResourceLoader();
    public class SourceFile {
        public File file;
        public String path;
        public boolean dirty = true;
    }
    
    private SourceFile lastSourceFile;
    private boolean loaded=false;
    private Map<String,Long> mtimes = new HashMap<>();
    private boolean updated=false;
    
    private class ResourceLoader {
        private String path;
        
        private InputStream getResourceAsStream(File file){
            String[] paths = path.split(Pattern.quote(File.pathSeparator));
            String fPath = file.getPath();
            for (String root : paths ){
                if ( fPath.indexOf(root) == 0 ){
                    return getResourceAsStream(fPath.substring(root.length()));
                }
            }
            return null;
        }
        
        private InputStream getResourceAsStream(String file){
            String[] paths = path.split(Pattern.quote(File.pathSeparator));
            for ( String root : paths ){
                File rootFile = new File(root);
                URL url = null;
                if ( rootFile.getName().endsWith(".mirah")){
                    lastModified = rootFile.lastModified();
                    lastSourceFile = new SourceFile();
                    lastSourceFile.file = rootFile;
                    lastSourceFile.path = file;
                    try {
                        url = rootFile.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(MirahClassIndex.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                } else if ( rootFile.getName().endsWith(".jar")){
                    try {
                        url = new URL("jar:"+rootFile.getPath()+"!"+file);
                        if ( url != null ){
                            lastModified = rootFile.lastModified();
                            
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(ASMClassLoader.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                } else if ( rootFile.isDirectory() ){
                    try {
                        File f = new File(rootFile, file);
                        
                        url = f.toURI().toURL();
                        if ( url != null ){
                            lastModified = f.lastModified();
                            lastSourceFile = new SourceFile();
                            lastSourceFile.file = f;
                            lastSourceFile.path = file;
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(ASMClassLoader.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                } 
                if ( url != null ){
                    try {
                        return url.openStream();
                    } catch (Exception ex){
                        
                    }
                }
            }
            return null;
        }
    }
    
    Map<String,SourceFile> index = new HashMap<>();
    
    public SourceFile findSourceFile(Type type){
        try {
            load(false);
        } catch (IOException ex) {
            Logger.getLogger(MirahClassIndex.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
       SourceFile out =  index.get(type.getInternalName());
       if ( out == null && !updated ){
            try {
                updateIndex();
            } catch (IOException ex) {
                Logger.getLogger(MirahClassIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
            updated = true;
            return findSourceFile(type);
            
       }
       return out;
        
    }
    
    public void indexFile(File file){
        String[] paths = loader.path.split(Pattern.quote(File.pathSeparator));
        String fPath = file.getPath();
        for (String root : paths ){
            if ( fPath.indexOf(root) == 0 ){
                indexFile(fPath.substring(root.length()));
                return;
            }
        }
        
    }
    
    public void indexFile(final String sourcePath){
       MirahParser parser = new MirahParser();
       InputStream contents = null;
       try {
            contents = loader.getResourceAsStream(sourcePath);
            final SourceFile sourceFile = lastSourceFile;
            long modified = sourceFile.file.lastModified();
            String sourceFilePath = sourceFile.file.getPath();
            if (mtimes.containsKey(sourceFilePath) && 
                    mtimes.get(sourceFilePath).longValue() >= modified){
                // This file hasn't been changed since it was last indexed
                return;
            }
            mtimes.put(sourceFilePath, modified);

            ArrayList<String> removes = new ArrayList<>();
            for ( Map.Entry<String,SourceFile> e : index.entrySet()){
                if ( e.getValue().path.equals(sourceFile.path)){
                    removes.add(e.getKey());
                }
            }

            for ( String k : removes ){
                index.remove(k);
            }

            StreamCodeSource source = new StreamCodeSource(
                    new File(sourcePath ).getName(), 
                    contents
            );
            //final LinkedList<ClassIndex.Node> stack = new LinkedList<>();
            Object ast = null;
            try {
                 ast = parser.parse(source);
            } catch ( SyntaxError err){
                err.printStackTrace();
                return;
            }

            if ( ast instanceof Node ){
                Node node = (Node)ast;
                node.accept(new NodeScanner(){

                    String packageName=null;


                    @Override
                    public boolean enterPackage(
                            mirah.lang.ast.Package node, 
                            Object arg) {
                        packageName = node.name().identifier();

                        return super.enterPackage(node, arg); 
                    }

                    @Override
                    public boolean enterClassDefinition(
                            ClassDefinition node, 
                            Object arg) {
                         String className = packageName.replaceAll("\\.", "/")+"/"+
                                 node.name().identifier();

                         index.put(className, sourceFile);

                        return super.enterClassDefinition(node, arg); 
                    }

                    @Override
                    public boolean enterInterfaceDeclaration(InterfaceDeclaration node, Object arg) {
                        return enterClassDefinition(node, arg);
                    }
                    
                    

                    @Override
                    public Object exitClassDefinition(
                            ClassDefinition node, 
                            Object arg) {
                        return super.exitClassDefinition(node, arg); 
                    }


                }, null);

            }
            sourceFile.dirty = false;
       } finally {
           if ( contents != null ){
               try {
                   contents.close();
               } catch ( Throwable t){}
           }
       }
    }
    
    
    public void setPath(String path){
        loader.path = path;
    }
    
    public String getPath(){
        return loader.path;
    }
    
    public void setDirty(File f){
        boolean found = false;
        for ( SourceFile sf : index.values()){
            if ( sf.file.equals(f)){
                sf.dirty = true;
                found = true;
            }
        }
        if ( !found ){
            indexFile(f.getPath());
        }
    }
    
    public void setDirty(Type type){
        SourceFile sf = index.get(type.getInternalName());
        if ( sf != null ){
            sf.dirty = true;
        }
    }
    
    public void writeIndex(File file) throws IOException {
        Properties props = new Properties();
        
        for ( Map.Entry<String,SourceFile> e : index.entrySet()){
            SourceFile sf = e.getValue();
            
            props.put(e.getKey(), 
                    sf.path+File.pathSeparator+
                    sf.file.getPath()+File.pathSeparator+
                    String.valueOf(sf.dirty)
            );
        }
        try (FileOutputStream fos = new FileOutputStream(file)){
            props.store(fos, "Mirah Class Index");
        }
    }
    
    public void readIndex(File file) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)){
            index.clear();
            props.load(fis);
            for ( Object key : props.keySet()){
                
                String className = (String)key;
                String val = props.getProperty(className);
                SourceFile sf = new SourceFile();
                String[] parts = val.split(
                        Pattern.quote(File.pathSeparator)
                );
                if ( parts.length > 0 ){
                    sf.path = parts[0];
                }
                if ( parts.length > 1 ){
                    sf.file = new File(parts[1]);
                }
                if ( parts.length > 2 ){
                    sf.dirty = Boolean.parseBoolean(parts[2]);
                }
                index.put(className, sf);
                
            }
        }
    }
    
    
    private void readMtimes(File f) throws IOException{
        try (ObjectInputStream ois = 
                new ObjectInputStream(new FileInputStream(f))){

            mtimes = (Map<String,Long>)ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MirahClassIndex.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void writeMtimes(File f) throws IOException {
        try (ObjectOutputStream oos = 
                new ObjectOutputStream(new FileOutputStream(f))){
            oos.writeObject(mtimes);
        }
    }
    
    public boolean load(boolean force) throws IOException{
        if ( force || !loaded ){
            loaded = true;
            String path = getPath();
            if ( path == null ){
                return false;
            }
            String[] parts = path.split(Pattern.quote(File.pathSeparator));
            File file = new File(parts[0], ".mirahindex");
            //if ( !file.exists()){
            //    return false;
            //}
            try {
                readIndex(file);
            } catch (FileNotFoundException fnfe){
                return false;
            }
            try {
                File mFile = new File(parts[0], ".mirahmtimes");
                
                readMtimes(mFile);
            } catch ( FileNotFoundException fnfe){
                
            }
            
            return true;
        }
        return false;
    }
    
    
    
    public boolean save() throws IOException {
        String path = getPath();
        if ( path == null ){
            return false;
        }
        String[] parts = path.split(Pattern.quote(File.pathSeparator));
        writeIndex(new File(parts[0], ".mirahindex"));
        writeMtimes(new File(parts[0], ".mirahmtimes"));
        return true;
    }
    
    public void updateIndex() throws IOException {
        load(false);
        String path = getPath();
        if ( path == null ){
            return;
        }
        String[] parts = path.split(Pattern.quote(File.pathSeparator));
        for ( String f : parts ){
            File file = new File(f);
            if ( !file.getName().endsWith(".jar")){
                try {
                    indexDirectory(file);
                } catch (FileNotFoundException fnfe){
                    
                }
            }
        }
        updated = true;
    }
    
    public void deleteIndex() throws IOException{
        
        String path = getPath();
        if ( path == null ){
            return;
        }
        String[] parts = path.split(Pattern.quote(File.pathSeparator));
        File file = new File(parts[0], ".mirahindex");
        file.delete();
        
        File mFile = new File(parts[0], ".mirahmtimes");
        mFile.delete();
       


    }
    
    public void indexDirectory(File f) throws IOException {
        indexDirectory(f, false);
    }
    
    private void indexDirectory(File f, boolean skipDirectoryCheck) throws IOException{
        load(false);
        if ( !skipDirectoryCheck && !f.isDirectory() ){
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(f.toPath())){
            for ( Path p : ds){
                if ( p.toFile().getName().endsWith(".mirah")){
                    indexFile(p.toFile());
                } else if ( p.toFile().getName().endsWith(".java")){
                    // do nothing here.
                } else if ( p.toFile().isDirectory() ){
                    indexDirectory(p.toFile(), true);
                }
            }
        } 
    }
}
