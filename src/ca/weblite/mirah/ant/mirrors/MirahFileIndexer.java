/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import mirah.impl.MirahParser;
import mirah.lang.ast.ClassDefinition;
import mirah.lang.ast.InterfaceDeclaration;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeScanner;
import mirah.lang.ast.Package;
import mirah.lang.ast.StreamCodeSource;

/**
 *
 * @author shannah
 */
public class MirahFileIndexer implements ClassIndex.Indexer{

    
    
    @Override
    public boolean canIndex(String path, String relPath, String pattern) {
        return path.endsWith(".mirah");
    }

    @Override
    public void index(String path, String relPath, InputStream contents, final ClassIndex index, String pattern) throws IOException {
       MirahParser parser = new MirahParser();
       StreamCodeSource source = new StreamCodeSource(new File(path).getName(), contents);
       final LinkedList<ClassIndex.Node> stack = new LinkedList<>();
       Object ast = parser.parse(source);
       if ( ast instanceof Node ){
           Node node = (Node)ast;
           node.accept(new NodeScanner(){

               String packageName=null;
               
               
               @Override
               public boolean enterPackage(Package node, Object arg) {
                   packageName = node.name().identifier();
                   
                   return super.enterPackage(node, arg); //To change body of generated methods, choose Tools | Templates.
               }

               @Override
               public boolean enterClassDefinition(ClassDefinition node, Object arg) {
                   
                    String simpleName  = node.name().identifier();
                    String internalName = simpleName;

                    if ( stack.isEmpty() ){
                        if (packageName != null ){
                            internalName = packageName.replaceAll("\\.", "/")+"/"+simpleName;
                        }

                    } else {
                        internalName = stack.peek().internalName+"$"+simpleName;
                    }
                    String path = internalName.replaceAll("/", ".").replaceAll("\\$", ".");
                    ClassIndex.Node cls = index.addClass(path, internalName);
                    stack.push(cls);
                    
                   return super.enterClassDefinition(node, arg); //To change body of generated methods, choose Tools | Templates.
               }

               @Override
               public Object exitClassDefinition(ClassDefinition node, Object arg) {
                   stack.pop();
                   return super.exitClassDefinition(node, arg); //To change body of generated methods, choose Tools | Templates.
               }

               
           }, null);
       
       }
       
       
    }

    @Override
    public boolean indexingRequiresFileContents(String path, String relPath, String pattern) {
        return true;
    }

    
    
}
