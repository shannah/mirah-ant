/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.util.HashMap;
import java.util.Map;
import mirah.lang.ast.Arguments;
import mirah.lang.ast.ClassDefinition;
import mirah.lang.ast.Import;
import mirah.lang.ast.MethodDefinition;
import mirah.lang.ast.Node;
import mirah.lang.ast.StaticMethodDefinition;
import mirah.lang.ast.TypeName;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.tool.Mirahc;
import org.mirah.typer.MethodFuture;
import org.mirah.typer.ResolvedType;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.TypeListener;
import org.mirah.util.Context;
import org.mirah.util.SimpleDiagnostics;

/**
 * This class is incomplete and may not be feasible.
 * @author shannah
 */
public class MirahToJava {
    
    static String src = "import java.util.List\n" +
"import java.util.ArrayList\n" +
            "import ca.weblite.UnknownClass\n"+
"\n" +
"class MyTestClass \nimplements java.io.Serializable, ca.weblite.MyOtherUnknownClass\n" +
            "class MyInternalClass < java.awt.event.ActionListener\n"+
 //           "    def actionPerformed(e:java.awt.event.ActionEvent):void\n"
 //           + "      'foobar'\n"
 //           +"   end\n"
            "end\n"+
"	def self.toList:void\n" +
"		UnknownClass.new\n" +
"	end\n" +
"	\n" +
"end";
    
    public static void main(String[] args){
        final StringBuilder sb = new StringBuilder();
        final Map<TypeFuture,String> placeholders = new HashMap<TypeFuture,String>();
        final Map<String,String> replacements = new HashMap<String,String>();
        Mirahc mirahc = new Mirahc();
        mirahc.addFakeFile("MyClass.mirah", src);
        
        //mirahc.setDiagnostics(new SimpleDiagnostics(false){
        //    
        //});
        mirahc.setDebugger(new DebuggerInterface(){

            @Override
            public void parsedNode(Node node) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void enterNode(Context cntxt, Node node, boolean bln) {
                System.out.println(node);
                if ( node instanceof Import ){
                    Import inode = (Import)node;
                    sb.append("\nimport ").append(inode.fullName().identifier()).append(";");
                } else if ( node instanceof ClassDefinition ){
                    ClassDefinition inode = (ClassDefinition)node;
                    sb.append("\npublic class ").append(inode.name().identifier()).append(" ");
                    if ( inode.superclass() != null ){
                        sb.append("extends ").append(inode.superclass().typeref().name());
                    }
                    if ( inode.interfaces_size() > 0 ){
                        int len = inode.interfaces_size();
                        sb.append("implements ");
                        for ( int i=0; i<len; i++){
                            TypeName type = inode.interfaces(i);
                            //System.out.println("INT:"+type.typeref().name());
                            sb.append(type.typeref().name());
                            if ( i < len-1 ){
                                sb.append(", ");
                            }
                        }
                        sb.append(" ");
                    }
                    sb.append("{\n");
                    
                } 
            }

            @Override
            public void exitNode(Context cntxt, Node node, TypeFuture tf) {
                if ( node instanceof ClassDefinition ){
                    sb.append("\n}\n");
                } else if ( node instanceof MethodDefinition ){
                    final MethodDefinition inode = (MethodDefinition)node;
                    int bodySize = inode.body_size();
                    System.out.println("Body "+bodySize);
                    for ( int i=0; i<bodySize; i++){
                        Node n = inode.body(i);
                        System.out.println("B:"+n);
                    }
                    if ( inode.type() == null ){
                        final String placeHolder = "%%placeholder%%"+placeholders.size();
                        sb.append("\n").append(placeHolder).append("\n");
                        placeholders.put(tf, placeHolder);
                        tf.onUpdate(new TypeListener(){

                            @Override
                            public void updated(TypeFuture tf, ResolvedType rt) {
                                MethodFuture mf = (MethodFuture)tf;
                                
                                StringBuilder sb2 = new StringBuilder();
                                
                                String returnType = null;
                                
                                if ( mf.returnType().inferredType().isError() ){
                                    System.out.println(mf.returnType().inferredType());
                                    //System.out.println("RT: "+mf.returnType());
                                    returnType = mf.returnType().inferredType().toString();
                                    System.out.println("RT:"+returnType);
                                    int idx = returnType.lastIndexOf(",");
                                    if ( idx != -1 ){
                                        returnType = returnType.substring(0, idx);
                                        System.out.println("RT2:"+returnType);
                                    }
                                    idx = returnType.lastIndexOf(" ");
                                    if ( idx != -1 ){
                                        returnType = returnType.substring(idx+1);
                                        System.out.println(returnType);
                                    }
                                } else {
                                    returnType = mf.returnType().inferredType().name();
                                }
                                System.out.println("Type is now "+returnType);
                                String sttc = "";
                                System.out.println("TR:"+inode);
                                if ( inode instanceof StaticMethodDefinition ){
                                    sttc = "static ";
                                }
                                
                                sb2.append("public ").append(sttc).append(returnType).append(" ").append(inode.name().identifier()).append("(").append("){}");
                                replacements.put(placeHolder, sb2.toString());
                                
                            }
                            
                        });
                    } else {
                        
                        String sttc = "";
                        System.out.println("TR:"+inode);
                        if ( inode instanceof StaticMethodDefinition ){
                            sttc = "static ";
                        }
                        sb.append("public ").append(sttc).append(inode.type().typeref().name()).append(" ").append(inode.name().identifier()).append("(").append("){}");
                        
                    }
                    
                }
            }
            
            

            @Override
            public void inferenceError(Context cntxt, Node node, TypeFuture tf) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                System.out.println("Inference err on "+node);
                
            }
            
        });
        
        mirahc.compile(new String[0]);
        String src = sb.toString();
        for ( Map.Entry<String,String> repl : replacements.entrySet() ){
            src = src.replace(repl.getKey(), repl.getValue());
        }
    
        
        System.out.println(src);
        
    }
}
