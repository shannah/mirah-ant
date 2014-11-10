/**
 * Copyright 2014 Steve Hannah
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ca.weblite.asm;

import ca.weblite.asm.MirahClassIndex.SourceFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import mirah.impl.MirahParser;
import mirah.lang.ast.ClassDefinition;
import mirah.lang.ast.Import;
import mirah.lang.ast.InterfaceDeclaration;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeScanner;
import mirah.lang.ast.StreamCodeSource;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class MirahClassLoader extends BaseClassLoader{
    
    
    private MirahClassIndex index = new MirahClassIndex();
    private final ASMClassLoader bytecodeLoader;
    
    private ClassNode lastFoundClass = null;
    
    private final Map<String,String> fakeFileQueue = new HashMap<>();
    
    public MirahClassLoader(Context context, ClassLoader parent){
        super(context, parent);
        bytecodeLoader = new ASMClassLoader(new Context(), null);
        index = new MirahClassIndex();
    }
    
    public void addFakeFile(String path, String contents) throws IOException {
        if ( index.getPath() != null ){
            String srcRoot = index.getPath();
            if ( srcRoot.indexOf(File.pathSeparator) != -1 ){
                srcRoot = srcRoot.
                        split(Pattern.quote(File.pathSeparator))[0];
            }
            File fakeRoot = new File(srcRoot, "._mirah_fake_files_cache");
            Map<String,String> toFlush = new HashMap<>();
            toFlush.put(path, contents);
            if ( !fakeFileQueue.isEmpty()){
                toFlush.putAll(fakeFileQueue);
                fakeFileQueue.clear();
            }
            for ( Map.Entry<String,String> e : toFlush.entrySet() ){
                File fakeFile = new File(fakeRoot, e.getKey());
                File fakeFileParent = fakeFile.getParentFile();
                fakeFileParent.mkdirs();
                
                try (FileOutputStream fos = new FileOutputStream(fakeFile)){
                    PrintWriter out = new PrintWriter(fos, true);
                    out.print(e.getValue());
                    out.close();
                }

            }
            
        }
    }
    
    
    public void clearFakeFileCache() throws IOException{
        if ( index.getPath() != null ){
            String srcRoot = index.getPath();
            if ( srcRoot.indexOf(File.pathSeparator) != -1 ){
                srcRoot = srcRoot.
                        split(Pattern.quote(File.pathSeparator))[0];
            }
            File fakeRoot = new File(srcRoot, "._mirah_fake_files_cache");
            if ( fakeRoot.exists()){
                Path directory = fakeRoot.toPath();
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, 
                                BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, 
                                IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                });
            }
            
        }
    }
    
    private ClassNode findClassImpl(final Type type){
        lastFoundClass = null;
        SourceFile sourceFile = index.findSourceFile(type);
        if ( sourceFile != null ){
            if ( !sourceFile.dirty ){
                ClassNode cached = bytecodeLoader.findClass(type);
                if ( cached != null && bytecodeLoader.getLastModified() > 
                        sourceFile.file.lastModified() ){
                    lastFoundClass = cached;
                    return cached;
                }
            }
            
            try (FileInputStream fis = new FileInputStream(sourceFile.file)){
                MirahParser parser = new MirahParser();
                StreamCodeSource source = new StreamCodeSource(
                   sourceFile.file.getPath(), 
                   fis
                );
                
                final LinkedList<ClassFinder> scopeStack = new LinkedList<>();
                Object ast = parser.parse(source);
                
                if ( ast instanceof Node ){
                    Node node = (Node)ast;
                    node.accept(new NodeScanner(){

                        String packageName=null;
                        

                        @Override
                        public boolean enterPackage(
                                mirah.lang.ast.Package node, 
                                Object arg) {
                            if ( lastFoundClass != null ){
                                return true;
                            }
                            packageName = node.name().identifier();
                            ClassFinder scope = new ClassFinder(
                                    context.get(ClassLoader.class), 
                                    null
                            );
                            scope.addImport(packageName+".*");
                            if ( !scopeStack.isEmpty()){
                                scopeStack.pop();
                            }
                            scopeStack.push(scope);
                            return super.enterPackage(node, arg); 
                        }

                        @Override
                        public boolean enterInterfaceDeclaration(InterfaceDeclaration node, Object arg) {
                            return enterClassDefinition(node, arg);
                        }

                        
                        
                        @Override
                        public boolean enterClassDefinition(
                                ClassDefinition node, 
                                Object arg) {
                             if ( lastFoundClass != null ){
                                 return true;
                             }
                             String className = packageName.
                                     replaceAll("\\.", "/")+"/"+
                                     node.name().identifier();
                             if ( type.getInternalName().equals(className)){
                                 ClassWriter writer = new ClassWriter(1);
                                 
                                 String superName = "java/lang/Object";
                                 if ( node.superclass() != null ){
                                     superName = node.superclass().typeref().
                                             name();
                                     ClassNode superClass = scopeStack.peek().
                                             findStub(superName);
                                     assert superClass!=null;
                                     superName = superClass.name;
                                 }
                                 
                                 
                                 List<String> interfaces = new ArrayList<>();
                                 if ( node.interfaces() != null ){
                                     int len = node.interfaces_size();
                                     for ( int i=0; i<len; i++){
                                          String iname = node.interfaces(i).
                                                  typeref().name();
                                          ClassNode iClass = scopeStack.peek().
                                                  findStub(iname);
                                          if ( iClass == null ){
                                              throw new RuntimeException("Failed to find interface "+iname+" in class definition of "+className);
                                          }
                                          interfaces.add(iClass.name);
                                     }
                                 }
                                 
                                 lastFoundClass = new ClassNode();
                                 lastFoundClass.version = 1;
                                 lastFoundClass.access = Opcodes.ACC_PUBLIC;
                                 lastFoundClass.name = type.getInternalName();
                                 lastFoundClass.superName = superName;
                                 lastFoundClass.interfaces = interfaces;
                                 
                                 writer.visit(
                                         1, 
                                         Opcodes.ACC_PUBLIC,
                                         type.getInternalName(),
                                         null,
                                         superName,
                                         interfaces.toArray(new String[0])
                                 );
                                 
                                 byte[] classBytes = writer.toByteArray();
                                 
                                 File classFilePath = new File(getCachePath(),
                                         type.getInternalName()+".class"
                                 );
                                 classFilePath.getParentFile().mkdirs();
                                 try (FileOutputStream fos = 
                                         new FileOutputStream(classFilePath)){
                                     fos.write(classBytes);
                                 } catch (IOException ex) {
                                     Logger.getLogger(
                                             MirahClassLoader.class.getName()).
                                             log(Level.SEVERE, null, ex);
                                 }
                             }
                             

                            return super.enterClassDefinition(node, arg); 
                        }

                        @Override
                        public Object exitClassDefinition(
                                ClassDefinition node, 
                                Object arg) {
                            return super.exitClassDefinition(node, arg); 
                        }

                        @Override
                        public boolean enterImport(Import node, Object arg) {
                            
                            if ( scopeStack.isEmpty()){
                                ClassFinder scope = new ClassFinder(
                                        context.get(ClassLoader.class), 
                                        null
                                );
                                scopeStack.push(scope);
                            }
                            scopeStack.peek().addImport(node.fullName().identifier());
                            //System.out.println("Entering import: "+node.fullName().identifier());
                            return super.enterImport(node, arg); 
                        }

                        

                    }, null);

                }
            } catch (IOException ex) {
                Logger.getLogger(MirahClassLoader.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            
            
            if ( lastFoundClass != null ){
                return lastFoundClass;
            }
            
        }
        return null;
    }
    
    
    @Override
    public ClassNode findClass(final Type type){
        ClassNode out = findClassImpl(type);
        if ( out != null ){
            return out;
        }
        return super.findClass(type);
    }
    
    @Override
    public ClassNode findStub(Type type){
        ClassNode out = findClassImpl(type);
        if ( out != null ){
            return out;
        }
        return super.findStub(type);
    }
    
    public void setCachePath(String path){
        bytecodeLoader.setPath(path);
    }
    
    public String getCachePath(){
        return bytecodeLoader.getPath();
    }
    
    public void setDirty(File file){
        index.setDirty(file);
    }
    
    public void setDirty(Type type){
        index.setDirty(type);
    }
    
    public void setPath(String path){
        index.setPath(path);
    }
    
    public String getPath(){
        return index.getPath();
    }
    
    public MirahClassIndex getIndex(){
        return index;
    }
}
