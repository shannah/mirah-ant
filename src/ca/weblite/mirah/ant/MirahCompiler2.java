/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import mirah.lang.ast.Import;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeFilter;
import org.mirah.jvm.mirrors.MirrorTypeSystem;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.TypeSystem;
import org.mirah.util.Context;
import org.mirah.util.SimpleDiagnostics;

/**
 * A "better" Mirah compiler.  This supports circular dependencies between
 * .java files and .mirah files. 
 * @author shannah
 */
public class MirahCompiler2 extends Mirahc {
    
    private Set<File> javaSourceDependencies = new HashSet<>();
    private Set<String> loadedIds = new HashSet<>();
    private boolean compileJavaSources = false;
    
    
    
    
    /**
     * Local debugger interface.  The default debugger interface is used by this class
     * to hook into the compile process and obtain the compile context, so we need
     * to provide an additional means for callers to add their own debugging.
     * 
     */
    private DebuggerInterface debugger = null;
    
    /**
     * List of java source files upon which the compiled files may depend.
     */
    final private List<File> javaSourceFiles = new ArrayList<>();
    
    /**
     * "Fake" java source files upon which the compiled mirah files may
     * depend.  Maps file names to file contents.
     */
    final private Map<String,String> fakeJavaSourceFiles = new HashMap<>();
    
    
    /**
     * Adds a .java source file or directory path to the list of files
     * that the .mirah files may depend on.  
     * @param src The path to a .java file or a directory which will be searched
     * recursively for .java files.
     */
    public void addJavaSourceFileOrDirectory(String src){
        javaSourceFiles.add(new File(src));
    }
    
    /**
     * Adds a "fake" java source file upon which the .mirah files may depend.
     * @param name The name of the fake file.
     * @param content The contents of the fake file.
     */
    public void addFakeJavaSourceFile(String name, String content){
        fakeJavaSourceFiles.put(name, content);
    }
    
    private static final FilenameFilter javaFileFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".java");
        }
        
    };
    
    private boolean isSourceChanged(File f){
        String pathFromRoot = getPathFromRoot(f);
        if ( pathFromRoot == null ){
            throw new RuntimeException("Path not in root");
        }
        String basePath = pathFromRoot.replace("\\.java$", "");
        long mtime = f.lastModified();
        for ( URL root : this.classpath()){
            File classFile = new File(root.getFile()+"/"+basePath+".class");
            if ( classFile.exists() && classFile.lastModified() > mtime ){
                return false;
            }
            
        }
        return true;
        
    }
    
    private String getPathFromRoot(File f){
        for ( URL root : this.classpath() ){
            if ( f.getPath().startsWith(root.getFile())){
                return f.getPath().substring(root.getFile().length());
            }
        }
        return null;
    }
    
    private void loadJavaSource(Context context, File f){
        System.out.println("Inside loadJavaSource "+f);
        MirrorTypeSystem typeSystem = (MirrorTypeSystem)context.get(TypeSystem.class);
        if ( f.isDirectory() ){
            for ( File jf : f.listFiles(javaFileFilter) ){
                System.out.println("Java file "+jf);
                loadJavaSource(context, jf);
            }
        } else {
            
            // Here we are loading a java source file into the context
            // First let's check to make sure that the source has changed
            // since the last build.
            if ( isSourceChanged(f) && !javaSourceDependencies.contains(f)){
                System.out.println("Loading "+f);
                javaSourceDependencies.add(f);
                JavaToMirahMirror sourceLoader = new JavaToMirahMirror(context);
                try {
                    sourceLoader.createMirahMirrors(f);
                } catch (IOException ex) {
                    Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    private void loadJavaSource(Context context, String identifier){
        if ( !loadedIds.contains(identifier) ){
            loadedIds.add(identifier);
            MirrorTypeSystem typeSystem = (MirrorTypeSystem)context.get(TypeSystem.class);
            String idPath = identifier.replaceAll("\\.", "/");
            
            // First check for fake files
            String fakeId1 = idPath+".java";
            
            for ( String filePath : fakeJavaSourceFiles.keySet()){
                String baseName = filePath;
                if ( baseName.indexOf("/") != -1 ){
                    baseName = baseName.substring(baseName.lastIndexOf("/")+1);
                }
                if ( fakeId1.equals(filePath) || filePath.equals(idPath+"/"+baseName)){
                    try {
                        File f = File.createTempFile(baseName, ".java");
                        f.deleteOnExit();
                        FileWriter fw = null;
                        try {
                            fw = new FileWriter(f);
                            fw.append(fakeJavaSourceFiles.get(filePath));
                            
                        } finally {
                            fw.close();
                        }
                        
                        JavaToMirahMirror sourceLoader = new JavaToMirahMirror(context);
                        
                        
                        sourceLoader.createMirahMirrors(f);
                        f.delete();
            
                    }   catch (IOException ex) {
                        Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            for ( URL srcRoot : this.classpath() ){
                File rootDir = new File(srcRoot.getFile());
                if ( rootDir.isDirectory() ){
                    File packageDir = new File(rootDir.getPath()+"/"+idPath);
                    if ( packageDir.exists() ){
                        loadJavaSource(context, packageDir);
                    } else {
                        File javaFile = new File(rootDir.getPath()+"/"+idPath+".java");
                        if ( javaFile.exists()){
                            loadJavaSource(context, javaFile);
                        }
                    }
                }
            }
        }
        
    }
    
    /**
     * Parses and loads the classes in the specified java source files into the
     * current compile context.
     * @param context The current compile context.
     */
    private void loadJavaSource(Context context){
        JavaToMirahMirror sourceLoader = new JavaToMirahMirror(context);
        try {
            List<File> javaSourceInputs = new ArrayList<File>();
            javaSourceInputs.addAll(javaSourceFiles);
            List<File> temps = new ArrayList<File>();
            for ( String name : fakeJavaSourceFiles.keySet()){
                File f = File.createTempFile(name, ".java");
                f.deleteOnExit();
                FileWriter fw = null;
                try {
                    fw = new FileWriter(f);
                    fw.append(fakeJavaSourceFiles.get(name));
                    
                } finally {
                    fw.close();
                }
                temps.add(f);
                
            }
            
            javaSourceInputs.addAll(temps);
            sourceLoader.createMirahMirrors(javaSourceInputs.toArray(new File[0]));
            for ( File f : temps ){
                f.delete();
            }
        } catch (IOException ex) {
            Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Installs the internal debugger that is used to hook into the compile process.
     */
    private void installDebugger(){
        
        
        super.setDebugger(new DebuggerInterface() {
            
            @Override
            public void parsedNode(Node node) {
               
               if ( debugger != null )debugger.parsedNode(node);
            }

            @Override
            public void enterNode(Context cntxt, Node node, boolean bln) {
                if ( debugger != null) debugger.enterNode(cntxt, node, bln);
            }

            @Override
            public void exitNode(Context context, Node node, TypeFuture tf) {
                if ( node instanceof Import ){
                    Import inode = (Import)node;
                    String className = inode.fullName().identifier();
                    loadJavaSource(context, className);
                    
                } else if ( node instanceof mirah.lang.ast.Package ){
                    
                    mirah.lang.ast.Package pnode = (mirah.lang.ast.Package)node;
                    String pkgName = pnode.name().identifier();
                    loadJavaSource(context, pkgName);
                }
                if ( debugger != null ) debugger.exitNode(context, node, tf);
            }

            @Override
            public void inferenceError(Context cntxt, Node node, TypeFuture tf) {
                if ( debugger != null ) debugger.inferenceError(cntxt, node, tf);
            }
        });
    }
    
    /**
     * Runs the mirah compile process.
     * @param args
     * @return 
     */
    @Override
    public int compile(String[] args) {
        javaSourceDependencies.clear();
        loadedIds.clear();
        installDebugger();
        int out = super.compile(args); 
        System.out.println(javaSourceDependencies);
        if ( compileJavaSources ){
            compileJavaSources();
        }
        return out;
    }
    
    
    private void compileJavaSources(){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StringBuilder sb = new StringBuilder();
        URL[] cps = this.classpath();
        for ( int i=0; i<cps.length; i++){
            sb.append(new File(cps[i].getFile()).getPath());
            if ( i < cps.length-1 ){
                sb.append(File.pathSeparator);
            }
        }
        int numFiles = javaSourceDependencies.size();
        String classPath = sb.toString();
        String[] args = new String[numFiles+4];
        int i = 0;
        //args[i++] = "javac";
        args[i++] = "-classpath";
        args[i++] = classPath;
        args[i++] = "-d";
        args[i++] = this.destination();
        
        for ( File dep : javaSourceDependencies ){
            args[i++] = dep.getPath();
        }
        compiler.run(System.in, System.out, System.err, args);
    }
    
    
    /**
     * Assigns a debugger to receive debug info.
     * @param debugger 
     */
    @Override
    public void setDebugger(DebuggerInterface debugger) {
        this.debugger = debugger;
        
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
