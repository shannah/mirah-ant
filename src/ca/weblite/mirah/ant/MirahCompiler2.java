/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mirah.lang.ast.Node;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.typer.TypeFuture;
import org.mirah.util.Context;

/**
 * A "better" Mirah compiler.  This supports circular dependencies between
 * .java files and .mirah files. 
 * @author shannah
 */
public class MirahCompiler2 extends Mirahc {
    
    /**
     * Flag indicating whether the java source files have been loaded yet on this compile cycle.
     */
    private boolean javaSourceLoaded = false;
    
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
                if ( !javaSourceLoaded ){
                    
                    loadJavaSource(cntxt);
                    javaSourceLoaded = true;
                }
                if ( debugger != null) debugger.enterNode(cntxt, node, bln);
            }

            @Override
            public void exitNode(Context cntxt, Node node, TypeFuture tf) {
                if ( debugger != null ) debugger.exitNode(cntxt, node, tf);
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
        javaSourceLoaded = false;
        installDebugger();
        return super.compile(args); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    /**
     * Assigns a debugger to receive debug info.
     * @param debugger 
     */
    @Override
    public void setDebugger(DebuggerInterface debugger) {
        this.debugger = debugger;
        
    }
    
    
    
}
