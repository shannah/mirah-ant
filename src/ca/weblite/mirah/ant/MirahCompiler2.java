/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import mirah.lang.ast.Import;
import mirah.lang.ast.Node;
import org.mirah.jvm.mirrors.MirrorTypeSystem;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.TypeSystem;
import org.mirah.util.Context;

/**
 * A "better" Mirah compiler.  This supports circular dependencies between
 * .java files and .mirah files. 
 * @author shannah
 */
public class MirahCompiler2 extends Mirahc {
    private String jvmVersion = null;
    private String bootClassPath = null;
    private Set<File> javaSourceDependencies = new HashSet<>();
    private Set<String> loadedIds = new HashSet<>();
    private boolean compileJavaSources = false;
    URL[] javaSourceClasspath = null;
    private String macroClasspath = null;
    private String destination = null;
    
   

    @Override
    public void setMacroClasspath(String classpath) {
        super.setMacroClasspath(classpath); //To change body of generated methods, choose Tools | Templates.
        macroClasspath = classpath;
    }

    @Override
    public void setDestination(String dest) {
        super.setDestination(dest); 
        destination = dest;
        
    }
    
    
    
    
   
    
    
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
    
    
    public void setJavaSourceClasspath(String path){
        
        if ( path == null || "".equals(path)){
            javaSourceClasspath = new URL[0];
            return;
        }
        String[] paths = path.split(File.pathSeparator);
        javaSourceClasspath = new URL[paths.length];
        for ( int i=0; i<paths.length; i++){
            try {
                javaSourceClasspath[i] = new File(paths[i]).toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public URL[] getJavaSourceClasspath(){
        return (javaSourceClasspath == null ) ? new URL[0] : javaSourceClasspath;
    }
    
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
        URL[] cp = this.getJavaSourceClasspath();
        if ( cp.length == 0 ) cp = this.classpath();
        for ( URL root : cp ){
            if ( f.getPath().startsWith(root.getFile())){
                return f.getPath().substring(root.getFile().length());
            }
        }
        return null;
    }
    
    private void loadJavaSource(Context context, File f){
        MirrorTypeSystem typeSystem = (MirrorTypeSystem)context.get(TypeSystem.class);
        if ( f.isDirectory() ){
            for ( File jf : f.listFiles(javaFileFilter) ){
                loadJavaSource(context, jf);
            }
        } else {
            
            // Here we are loading a java source file into the context
            // First let's check to make sure that the source has changed
            // since the last build.
            if ( isSourceChanged(f) && !javaSourceDependencies.contains(f)){
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
            
            URL[] cp = this.getJavaSourceClasspath();
            if ( cp.length == 0 ) cp = this.classpath();
            
            for ( URL srcRoot : cp ){
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
        List<String> filteredArgs = new ArrayList<String>();
        //filteredArgs.add("--vmodule");
        //filteredArgs.add("org.mirah.jvm.mirrors.BytecodeMirrorLoader=ALL");
        for ( int i=0; i<args.length; i++){
            if ( "--javac:sources".equals(args[i])){
                this.setJavaSourceClasspath(args[i+1]);
                i++;
            } else {
                filteredArgs.add(args[i]);
            }
        }
        
        args = filteredArgs.toArray(new String[0]);
        javaSourceDependencies.clear();
        loadedIds.clear();
        installDebugger();
        
        
        // Deal with macros in the macroclasspath
        if ( macroClasspath != null ){
            StringBuilder fakeFileContents = new StringBuilder();
            String[] paths = macroClasspath.split(File.pathSeparator);
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/macros/Bootstrap.class");
            final List<String> bootstrapClasses = new ArrayList<String>();
            for ( final String path : paths ){
                
                final File file = new File(path);
                if ( file.isDirectory() ){
                    final Path root = file.toPath();
                    Path p = file.toPath();
                    try {
                        Files.walkFileTree(p, new SimpleFileVisitor<Path>(){

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if ( matcher.matches(file)){
                                    bootstrapClasses.add(root.relativize(file.getParent()).toFile().getPath().replaceAll(File.separator, "::"));

                                }
                                return super.visitFile(file, attrs);
                            }

                        });
                    } catch (IOException ex) {
                        Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if ( path.endsWith(".jar")){
                    ZipInputStream zip = null;
                    try {
                        zip = new ZipInputStream(new FileInputStream(path));
                        for(ZipEntry entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry())
                            if(entry.getName().endsWith("macros/Bootstrap.class") && !entry.isDirectory()) {
                                
                                String[] parts = entry.getName().split("/");
                                StringBuilder pkgName = new StringBuilder();
                                for ( int i=0; i<parts.length-1; i++){
                                    pkgName.append(parts[i]);
                                    if ( i < parts.length-2 ){
                                        pkgName.append("::");
                                    }
                                   
                                }
                                bootstrapClasses.add(pkgName.toString());
                            }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            zip.close();
                        } catch (IOException ex) {
                            Logger.getLogger(MirahCompiler2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                
                
            }
            
            for ( String pkgName : bootstrapClasses ){
                
                fakeFileContents.append("\n")
                        .append(pkgName)
                        .append("::Bootstrap::loadExtensions");
            }
            
            this.addFakeFile("MacrosBootstrap.mirah", fakeFileContents.toString());
        }
        
        
        int out = super.compile(args); 
        
        File macrosBootstrapOutput = new File(destination, "MacrosBootstrap.class");
        if ( macrosBootstrapOutput.exists() ){
            macrosBootstrapOutput.delete();
        }
        
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
        String classPath = sb.toString();
        
        
        int numFiles = javaSourceDependencies.size();
        
        URL[] scps = this.getJavaSourceClasspath();
        int numArgs = numFiles+4;
        
        String javaSourcePath = null;
        if ( scps.length > 0 ){
            numArgs += 2;
            sb = new StringBuilder();
            for ( int i=0; i<scps.length; i++){
                sb.append(new File(scps[i].getFile()).getPath());
                if ( i < scps.length-1 ){
                    sb.append(File.pathSeparator);
                }
            }
            javaSourcePath = sb.toString();
            
        }
        
        if ( bootClassPath != null ){
            numArgs+=2;
        }
        
        if ( jvmVersion != null ){
            numArgs +=2;
        }
       
        String[] args = new String[numArgs];
        int i = 0;
        //args[i++] = "javac";
        args[i++] = "-classpath";
        args[i++] = classPath;
        args[i++] = "-d";
        args[i++] = this.destination();
        if ( scps.length > 0 ){
            args[i++] = "-sourcepath";
            args[i++] = javaSourcePath;
        }
        if ( bootClassPath != null ){
            args[i++] = "-bootclasspath";
            args[i++] = bootClassPath;
        }
        if ( jvmVersion != null ){
            args[i++] = "-target";
            args[i++] = jvmVersion;
        }
        
        for ( File dep : javaSourceDependencies ){
            args[i++] = dep.getPath();
        }
        
        
        compiler.run(System.in, System.out, System.err, args);
    }

    @Override
    public void setBootClasspath(String classpath) {
        bootClassPath = classpath;
        super.setBootClasspath(classpath); //To change body of generated methods, choose Tools | Templates.
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

    @Override
    public void setJvmVersion(String version) {
        jvmVersion = version;
        super.setJvmVersion(version); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    
    
}
