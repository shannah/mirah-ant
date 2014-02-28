/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.tools.Diagnostic;
import mirah.lang.ast.Node;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.typer.TypeFuture;
import org.mirah.util.Context;
import org.mirah.util.SimpleDiagnostics;

/**
 * @author shannah
 */
public class MirahcTask extends Task {
    
    private Path classPath;
    private Path macroClassPath;
    private Path bootClassPath;
    private Path src;
    private Path dest;
    
    
    public @Override
    void execute() throws BuildException {
        final List<String> messages = new ArrayList<String>();
        Mirahc c = new Mirahc();
        
        c.setDebugger(new DebuggerInterface(){

            @Override
            public void parsedNode(Node node) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void enterNode(Context cntxt, Node node, boolean bln) {
                cntxt.get(null)
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void exitNode(Context cntxt, Node node, TypeFuture tf) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void inferenceError(Context cntxt, Node node, TypeFuture tf) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                System.out.println("Inference error "+cntxt+" "+node+" "+tf);
                System.out.println(tf.resolve());
            }
            
        });
        
        if ( getBootClassPath() != null ){
            c.setBootClasspath(getBootClassPath().toString());
        }
        if ( getClassPath() != null ){
            c.setClasspath(getClassPath().toString());
        }
        if ( getMacroClassPath() != null ){
            c.setMacroClasspath(getMacroClassPath().toString());
        }
        if ( getDest() != null ){
            c.setDestination(getDest().toString());
        }
        
        List<File> mirahStubs = new ArrayList<File>();
        Iterator<Resource> it = getSrc().iterator();
        int i = 0;
        JavaToMirah stubber = new JavaToMirah();
        while ( it.hasNext()){
            
            Resource nex = it.next();
            if ( nex instanceof FileResource ){
                final List<String> localMessages = new ArrayList<String>();
                c.setDiagnostics(new SimpleDiagnostics(true){

                    @Override
                    public void log(Diagnostic.Kind kind, String position, String message) {
                        localMessages.add(kind.toString()+": "+position+" : "+message);
                    }

                });
                FileResource file = (FileResource)nex;
                c.addFileOrDirectory(file.getFile());
                boolean success = false;
                try {
                    if ( c.compile(new String[0]) == 0 ){
                        success = true;
                    }
                    
                } catch ( Exception ex){
                    // Failed to compile for some reason
                    localMessages.add(ex.getMessage());
                    
                }
                
                if ( !success ){
                    
                    File mirahStub = new File(file.getFile(), "MirahStub"+i+".mirah");
                    try {
                        stubber.createMirahStubs(file.getFile(), mirahStub);
                        if ( c.compile(new String[0]) == 0 ){
                            success = true;
                            
                        }

                    } catch ( Exception e){
                        localMessages.add(e.getMessage());
                    }
                }
                
                if ( !success ){
                    messages.addAll(localMessages);
                }
  
            }
            for ( String msg : messages ){
                System.err.println(msg);
            }
            
        }
        
        
        
        
        
        
        //stubber.createMirahStubs(src, mirahStub);
    }

    /**
     * @return the classPath
     */
    public Path getClassPath() {
        return classPath;
    }

    /**
     * @param classPath the classPath to set
     */
    public void setClassPath(Path classPath) {
        this.classPath = classPath;
    }

    /**
     * @return the macroClassPath
     */
    public Path getMacroClassPath() {
        return macroClassPath;
    }

    /**
     * @param macroClassPath the macroClassPath to set
     */
    public void setMacroClassPath(Path macroClassPath) {
        this.macroClassPath = macroClassPath;
    }

    /**
     * @return the bootClassPath
     */
    public Path getBootClassPath() {
        return bootClassPath;
    }

    /**
     * @param bootClassPath the bootClassPath to set
     */
    public void setBootClassPath(Path bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    /**
     * @return the src
     */
    public Path getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(Path src) {
        this.src = src;
    }

    /**
     * @return the dest
     */
    public Path getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(Path dest) {
        this.dest = dest;
    }
    
    
    
}
