package ca.weblite.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.util.SimpleDiagnostics;

/**
 *
 * @author shannah
 */
public class WLMirahCompiler {
    private Mirahc mirahc;
    private JavaExtendedStubCompiler javaStubCompiler;
    private ASMClassLoader baseClassLoader;
    private JavaSourceClassLoader javaSourceClassLoader;
    private MirahClassLoader mirahClassLoader;
    private Context context;
    private boolean setupComplete=false;
    private boolean precompileJavaStubs=true;
    
    
    private String sourcePath;
    private File destinationDirectory;
    private String classPath;
    private String macroClassPath;
    private String bootClassPath;
    private File classCacheDirectory;
    private File javaStubDirectory;
    private String jvmVersion;
    private SimpleDiagnostics diagnostics;
    private DebuggerInterface debugger;
    
    private final Map<String,String> fakeFiles = new HashMap<>();
    
    
    private void setupParameters(){
        if ( sourcePath == null ){
            sourcePath = System.getProperty(
                    "mirahc.source.path", 
                    "src/main/java"
            );
        }
        if ( destinationDirectory == null ){
            destinationDirectory = 
                    new File("mirahc.build.dir", "build/classes");
        }
        
        if ( classPath == null ){
            classPath = System.getProperty("mirahc.class.path", 
                            System.getProperty("java.class.path", ".")
            );
        }
        
        if ( macroClassPath == null ){
            macroClassPath = 
                    System.getProperty("mirahc.macro.class.path", classPath);
        }
        
        if ( bootClassPath == null ){
            bootClassPath = System.getProperty("mirahc.boot.class.path",
                    System.getProperty("sun.boot.class.path", null)
            );
        }
        
        if ( classCacheDirectory == null ){
            classCacheDirectory = new File(
                    System.getProperty("mirahc.mirah.stub.dir",
                            "build/mirah_cache"
                    )
            );
        }
        
        if ( javaStubDirectory == null ){
            javaStubDirectory = new File(
                    System.getProperty("mirahc.java.stub.dir",
                            "build/mirah_stubs"
                    )
            );
        }
        
        if ( jvmVersion == null ){
            jvmVersion = System.getProperty("mirahc.jvm.version", null);
        }
        
    }
    
    
    private void setup(){
        if ( !setupComplete ){
            setupComplete = true;
            setupParameters();
            context = new Context();
            
            // Setup the base class loader
            baseClassLoader = new ASMClassLoader(context, null);
            StringBuilder sb = new StringBuilder();
            if ( bootClassPath != null ){
                sb.append(bootClassPath).append(File.pathSeparator);
            }
            if ( classPath != null ){
                sb.append(classPath).append(File.pathSeparator);
            }
            baseClassLoader.setPath(sb.substring(0, sb.length()-1));
            
            // Setup the java source class loader
            
            ASMClassLoader cacheClassLoader = 
                    new ASMClassLoader(new Context(), null);
            cacheClassLoader.setPath(classCacheDirectory.getPath());
            
            javaSourceClassLoader = new JavaSourceClassLoader(
                    context,
                    baseClassLoader,
                    cacheClassLoader
            );
            
            
            javaSourceClassLoader.setPath(sourcePath);
            
            // Setup the mirah source class loader
            mirahClassLoader = 
                    new MirahClassLoader(context, javaSourceClassLoader);
            mirahClassLoader.setCachePath(classCacheDirectory.getPath());
            mirahClassLoader.setPath(sourcePath);
            try {
                mirahClassLoader.clearFakeFileCache();
            } catch (IOException ex){
                throw new RuntimeException(ex);
            }
            for ( Map.Entry<String,String> e : fakeFiles.entrySet()){
                try {
                    mirahClassLoader.addFakeFile(e.getKey(), e.getValue());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                mirahClassLoader.getIndex().updateIndex();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            
            // Setup the stub compiler
            javaStubCompiler = new JavaExtendedStubCompiler(context);
            
            mirahc = new Mirahc();
            if ( jvmVersion != null ){
                getMirahc().setJvmVersion(jvmVersion);
            }
            getMirahc().setBootClasspath(bootClassPath);
            //System.out.println("Bootclasspath is "+bootClassPath);
            String cp = javaStubDirectory.getPath()+File.pathSeparator+classPath+(macroClassPath!=null?(File.pathSeparator+macroClassPath):"");
            getMirahc().setClasspath(cp);
            //System.out.println("Compiling with classpath "+cp);
            getMirahc().setMacroClasspath(
                    javaStubDirectory.getPath()+File.pathSeparator+
                            cp+
                            File.pathSeparator+
                            macroClassPath
            );
            //System.out.println("Macro classpath is "+javaStubDirectory.getPath()+File.pathSeparator+
                            //macroClassPath);
            getMirahc().setDestination(destinationDirectory.getPath());
            if ( getDiagnostics() != null ){
                getMirahc().setDiagnostics(getDiagnostics());
            }
            if ( getDebugger() != null ){
                getMirahc().setDebugger(getDebugger());
            }
            for ( Map.Entry<String,String> e : fakeFiles.entrySet()){
                getMirahc().addFakeFile(e.getKey(), e.getValue());
            }

        }
    }
    
    public int compile(String[] args) throws IOException {
        //System.out.println("Compiling the following files: ");
        //for ( String arg : args){
        //    System.out.println(arg);
        //}
        setup();
        if ( precompileJavaStubs ){
            //System.out.println("Precompiling java stubs");
            String[] sourceDirs = 
                    sourcePath.split(Pattern.quote(File.pathSeparator));
            for ( String sourceDir : sourceDirs ){
                javaStubCompiler.compileDirectory(
                        new File(sourceDir), 
                        new File(sourceDir),
                        javaStubDirectory, 
                        true
                );
                
            }
            //System.out.println("Finished precompiling java stubs");
        }
        
        // Deal with macros in the macroclasspath
        if ( macroClassPath != null ){
            
            StringBuilder fakeFileContents = new StringBuilder();
            String[] paths = macroClassPath.split(File.pathSeparator);
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
                        Logger.getLogger(WLMirahCompiler.class.getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(WLMirahCompiler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(WLMirahCompiler.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            zip.close();
                        } catch (IOException ex) {
                            Logger.getLogger(WLMirahCompiler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                
                
            }
            
            for ( String pkgName : bootstrapClasses ){
                
                fakeFileContents.append("\n")
                        .append(pkgName)
                        .append("::Bootstrap::loadExtensions");
            }
            
            getMirahc().addFakeFile("MacrosBootstrap.mirah", fakeFileContents.toString());
        }
        
        //System.out.println("About to print directory "+javaStubDirectory);
        //printDirectory(javaStubDirectory);
        return getMirahc().compile(args);
        
        
    }
    
    public void printDirectory(File f){
        if ( f.isDirectory()){
            for ( File child : f.listFiles()){
                if ( child.isDirectory()){
                    printDirectory(child);
                } else {
                    System.out.println(child);
                }
            }
        }
    }
    
    public void setJavaStubDirectory(File directory){
        this.javaStubDirectory=directory;
    }
    
    public void setSourcePath(String path){
        this.sourcePath=path;
    }
    
    public void setDestinationDirectory(File directory){
        this.destinationDirectory=directory;
    }
    
    public void setClassPath(String path){
        this.classPath=path;
    }
    
    public void setBootClassPath(String path){
        this.bootClassPath=path;
    }
    
    public void setMacroClassPath(String path){
        this.macroClassPath=path;
    }
    
    public void setClassCacheDirectory(File dir){
        this.classCacheDirectory=dir;
    }
    
    public void setPrecompileJavaStubs(boolean set){
        this.precompileJavaStubs=set;
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
     * @return the diagnostics
     */
    public SimpleDiagnostics getDiagnostics() {
        return diagnostics;
    }

    /**
     * @param diagnostics the diagnostics to set
     */
    public void setDiagnostics(SimpleDiagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    /**
     * @return the debugger
     */
    public DebuggerInterface getDebugger() {
        return debugger;
    }

    /**
     * @param debugger the debugger to set
     */
    public void setDebugger(DebuggerInterface debugger) {
        this.debugger = debugger;
    }

    /**
     * @return the mirahc
     */
    public Mirahc getMirahc() {
        return mirahc;
    }
    
    public void addFakeFile(String name, String contents){
        fakeFiles.put(name, contents);
    }
}

