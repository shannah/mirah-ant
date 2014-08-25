/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author shannah
 */
public class ClassReader {
    
    
    
    
    
    private ClassNode node;
    private File sourceFile;
    LinkedList<Scope> scopeStack = new LinkedList<>();
    LinkedList<ClassNode> classNodeStack = new LinkedList<>();
    PackageScope packageScope;
    
    
    public ClassReader(ClassNode node, File sourceFile, ResourceLoader loader){
        Scope baseScope = new Scope(null);
        baseScope.setLoader(loader);
        scopeStack.add(baseScope);
        this.node = node;
        this.sourceFile = sourceFile;
        
    }
    
    
    
    
    
    /**
     * Wrapper for java file object so that it can be read by the TreeScanner
     * 
     */
    private static class MyFileObject extends SimpleJavaFileObject {

        private String src;

        public MyFileObject(File file) throws IOException {
            super(file.toURI(), JavaFileObject.Kind.SOURCE);
            src = readFile(file.getPath());
        }

        private String readFile(String file) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return src;
        }

    }
    
    /**
     * Generates the the mirah mirrors for a specific java source file.  This 
     * file must be a .java file.  It cannot be a directory.
     * 
     * @param javaSourceFile
     * @throws IOException 
     */
    public void parse() throws IOException {
        
        
        
        File javaSourceFile = sourceFile;
        JavaCompiler compiler = JavacTool.create();
        MyFileObject[] fos = new MyFileObject[]{
            new MyFileObject(javaSourceFile)
        };
        
        JavacTask task = (JavacTask) compiler.getTask(
                null, null, null, null, null, 
                Arrays.asList(fos)
        );
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        final Stack<Set<String>> typeParams = new Stack<Set<String>>();
        TreePathScanner scanner;
        scanner = new TreePathScanner() {

            private boolean isGenericType(String type) {
                Stack<Set> desk = new Stack<Set>();
                boolean found = false;
                while (!typeParams.empty()) {
                    Set params = typeParams.pop();
                    desk.push(params);
                    if (params.contains(type)) {
                        found = true;

                        break;
                    }

                }
                while (!desk.empty()) {
                    typeParams.push(desk.pop());
                }
                return found;
            }

            String formatType(String type) {
                int pos = type.indexOf("<");
                if (pos >= 0) {
                    type = type.substring(0, pos);
                }
                if (isGenericType(type)) {
                    return "Object";
                } 
                return type;
            }
            
            

            @Override
            public Object visitCompilationUnit(
                    CompilationUnitTree cut, 
                    Object p) {
                PackageScope scope = new PackageScope(
                        scopeStack.peek(), 
                        cut.getPackageName().toString()
                );
                scope.addImport(scope.getPackageName()+".*");
                scope.addImport("java.lang.*");
                scope.addImport("*");
                packageScope = scope;
                scopeStack.push(scope);
                return super.visitCompilationUnit(cut, p); 

            }

            @Override
            public Object visitImport(ImportTree it, Object p) {
                
                String path = it.getQualifiedIdentifier().toString();
                packageScope.addImport(path);
                
                return super.visitImport(it, p);
            }

            
            private void decorateClassNode(ClassTree ct, ClassNode classNode){
                int flags = getFlags(ct.getModifiers().getFlags());
                switch (ct.getKind()) {
                    case INTERFACE:
                        flags |= Opcodes.ACC_INTERFACE;
                        break;

                    case ENUM:
                        flags |= Opcodes.ACC_ENUM;
                        break;

                }
                classNode.access = flags;
                classNode.sourceFile = sourceFile.getPath();
                String extendsClause = "java.lang.Object";
                if ( ct.getExtendsClause() != null ){
                    extendsClause = ct.getExtendsClause().toString();
                }
                
                String superPath = scopeStack.
                        peek().
                        resolveName(
                                extendsClause, 
                                false
                        );
                
                ClassIndex.Node superNode = scopeStack.peek().getLoader().
                        find(superPath);
                        
                if ( superNode == null ){
                    throw new RuntimeException("Failed to find super class "+
                            superPath
                    );
                }
                
                classNode.superName = superNode.internalName;
                
                classNode.interfaces = new ArrayList<String>();
                String impl = ct.getImplementsClause().toString();
                if (!"".equals(impl)) {
                    String[] interfaces = impl.split(",");
                    for (String iface : interfaces) {
                        iface = iface.trim();  
                        String ipath = scopeStack.peek().
                                resolveName(iface, false);
                        ClassIndex.Node iNode = scopeStack.peek().
                                getLoader().find(ipath);
                        if ( iNode == null ){
                            throw new RuntimeException("Failed to load "+
                                    "interface "+ipath
                            );
                        }
                        
                        classNode.interfaces.add(iNode.internalName);
                    }
                }
            }
            
            
            @Override
            public Object visitClass(ClassTree ct, Object p) {  
                ClassScope clsScope = null;
                ClassNode currClassNode  = null;
                if ( scopeStack.peek() == packageScope ){
                    String path = packageScope.getPackageName()+
                            "."+ct.getSimpleName();
                    ClassIndex.Node n = packageScope.getLoader().find(path);
                    if ( n == null ){
                        throw new RuntimeException(
                                "Failed to find class "+path
                        );
                    }
                    node.name = n.internalName;
                    currClassNode = node;
                    decorateClassNode(ct, node);
                    
                    clsScope = new ClassScope(scopeStack.peek(), n);
                    
                } else {
                    // This must be an internal class
                    ClassNode parentClass = classNodeStack.peek();
                    if ( parentClass.innerClasses == null ){
                        parentClass.innerClasses = new ArrayList<>();
                    }
                    String path = scopeStack.peek().resolveName(
                            ct.getSimpleName().toString(), 
                            false
                    );
                    
                    ClassIndex.Node n = packageScope.getLoader().find(path);
                    if ( n == null ){
                        throw new RuntimeException(
                                "Failed to find class "+path
                        );
                    }
                    InnerClassNode innerNode = new InnerClassNode(
                            n.internalName,
                            parentClass.name,
                            n.simpleName,
                            getFlags(ct.getModifiers().getFlags())
                    );
                    parentClass.innerClasses.add(innerNode);
                    
                    ClassNode cls = new ClassNode();
                    cls.name = n.internalName;
                    cls.outerClass = parentClass.name;
                    
                    decorateClassNode(ct, cls);
                    clsScope = new ClassScope(scopeStack.peek(), n);
                    currClassNode = cls;
                }
                
                scopeStack.push(clsScope);
                classNodeStack.push(currClassNode);
                Object out = super.visitClass(ct, p);
                classNodeStack.pop();
                scopeStack.pop();
                
                
                return out;
               
            }

            /**
             * Converts modifier flags from Javac Tree into int flags usable in 
             * TypeMirror
             * @param mods
             * @return 
             */
            int getFlags(Set<Modifier> mods) {
                int flags = 0;
                for (Modifier m : mods) {
                    switch (m) {
                        case ABSTRACT:
                            flags |= Opcodes.ACC_ABSTRACT;
                            break;
                        case FINAL:
                            flags |= Opcodes.ACC_FINAL;
                            break;
                        case PRIVATE:
                            flags |= Opcodes.ACC_PRIVATE;
                            break;
                        case PROTECTED:
                            flags |= Opcodes.ACC_PROTECTED;
                            break;
                        case PUBLIC:
                            
                            flags |= Opcodes.ACC_PUBLIC;
                            break;
                        case STATIC:
                            flags |= Opcodes.ACC_STATIC;
                            break;                            
                    }
                }
                
                return flags;
            }

            @Override
            public Object visitVariable(final VariableTree vt, Object p) {
                String typeName = vt.getType().toString();
                String typePath = scopeStack.peek().
                        resolveName(vt.getType().toString(), false);
                String typeDescriptor = scopeStack.peek().
                        resolveName(vt.getType().toString(), true);
                String name = vt.getName().toString();
                int flags = getFlags(vt.getModifiers().getFlags());
                
                FieldNode fieldNode = new FieldNode(
                        flags,
                        name,
                        typeDescriptor,
                        null,
                        null
                );
                        
                ClassNode cls = classNodeStack.peek();
                if ( cls.fields == null ){
                    cls.fields = new ArrayList<FieldNode>();
                    
                }
                cls.fields.add(fieldNode);
                
                return super.visitVariable(vt, p); 
            }
            

            String generateMethodDescriptor(MethodTree mt){
                StringBuilder sb = new StringBuilder();
                sb.append("(");
                for ( VariableTree v : mt.getParameters()){
                    String type = scopeStack.peek().resolveName(
                            v.getType().toString(), 
                            true
                    );
                    sb.append(type);
                    
                }
                sb.append(")");
                String returnType = scopeStack.peek().resolveName(
                        mt.getReturnType().toString(), 
                        true
                );
                sb.append(returnType);
                
                return sb.toString();
            }
            
            @Override
            public Object visitMethod(final MethodTree mt, Object p) {
                
                MethodNode m = new MethodNode();
                
                int flags = getFlags(mt.getModifiers().getFlags());
                m.access = flags;
                m.desc = generateMethodDescriptor(mt);
                m.name = mt.getName().toString();
                ClassNode cls = classNodeStack.peek();
                if ( cls.methods == null ){
                    cls.methods = new ArrayList<MethodNode>();
                }
                cls.methods.add(m);
                
                return mt;
            }
        };
        scanner.scan(asts, null);

    }
    
}
