/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.mirah.ant;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
//import org.jruby.org.objectweb.asm.Opcodes;
import org.mirah.jvm.mirrors.AsyncMember;
import org.mirah.jvm.mirrors.JVMScope;
import org.mirah.jvm.mirrors.Member;
import org.mirah.jvm.mirrors.MirahMethod;
import org.mirah.jvm.mirrors.MirahMirror;
import org.mirah.jvm.mirrors.MirrorFuture;
import org.mirah.jvm.mirrors.MirrorProxy;
import org.mirah.jvm.mirrors.MirrorType;
import org.mirah.jvm.mirrors.MirrorTypeSystem;
import org.mirah.jvm.types.MemberKind;
import org.mirah.typer.AssignableTypeFuture;
import org.mirah.typer.ResolvedType;
import org.mirah.typer.Scoper;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.Typer;
import org.mirah.util.Context;
import org.objectweb.asm.Opcodes;

/**
 * Converts Java Source files into MirahMirrors
 * @author shannah
 */
public class JavaToMirahMirror {
    private Context mirahContext;
    private Typer typer;
    private MirrorTypeSystem typeSystem;
    private String currPackage = "";
    private Stack<MirahMirror> mirrorStack = new Stack<MirahMirror>();
    private JVMScope scope;
    private JVMScope internalScope;
    private Scoper scoper;
    private LinkedList<String> namespaceStack = new LinkedList<String>();

    /**
     * Creates a new converter that is intended to add MirahMirrors into the 
     * provided context.
     * @param context The compile context into which the MirahMirrors should
     * be added.
     */
    public JavaToMirahMirror(Context context) {
        mirahContext = context;
        typer = (Typer) context.get(Typer.class);
        
        typeSystem = (MirrorTypeSystem) typer.type_system();
        scoper = (Scoper)context.get(Scoper.class);/*new SimpleScoper(new ScopeFactory(){

            @Override
            public Scope newScope(Scoper scoper, Node node) {
                JVMScope scope = new JVMScope(scoper);
                scope.context_set(node);
                return scope;
            }
            
        });*/
               
        scope = new JVMScope(scoper);
        
        
    }
    
   

    /**
     * A utility class to walk a file tree.  Used to get all java source
     * files that need to be added.
     */
    private static class Filewalker {

        protected void visitFile(File f) {

        }

        public void walk(String path) {

            File root = new File(path);
            File[] list = root.listFiles();

            if (list == null) {
                return;
            }

            for (File f : list) {
                if (f.isDirectory()) {
                    walk(f.getAbsolutePath());
                    
                } else {
                    visitFile(f);
                    
                }
            }
        }

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
     * Imports a set of Java source files into the current Mirah compile context.
     * These files can be directories or files.  Directories will be searched recursively
     * for .java files.
     * @param srcRoot  The java source files to be imported (or directories which are searched recursively).
     * @throws IOException 
     */
    public void createMirahMirrors(File... srcRoot) throws IOException {
        final List<File> javaSourceFiles = new ArrayList<File>();
        Filewalker walker = new Filewalker() {

            @Override
            protected void visitFile(File f) {
                if (f.getName().endsWith(".java")) {
                    try {
                        createMirahMirrors(f);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };

        for (File f : srcRoot) {
            if (f.isDirectory()) {
                walker.walk(f.getPath());
            } else {
                generateMirahMirror(f);
            }
        }

    }

    /**
     * Checks if any of the input files have changed since the last cache file
     * was created.
     *
     * @param inputs
     * @param cache
     * @return
     */
    public boolean isChanged(File[] inputs, File cache) {
        if (!cache.exists()) {
            return true;
        }
        long mtime = cache.lastModified();
        for (File f : inputs) {
            if (f.lastModified() > mtime) {
                return true;
            }
        }
        return false;

    }

    private String getCurrentNamespacePrefix(){
        if ( namespaceStack.isEmpty() ){
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = namespaceStack.descendingIterator();
        boolean first = true;
        while ( it.hasNext()){
            String part = it.next();
            sb.append(part);
            if ( first ){
                first = false;
                
                sb.append(".");
            } else {
                sb.append("$");
            }
        }
        return sb.toString();
    }
    
    
    
    /**
     * Generates the the mirah mirrors for a specific java source file.  This file
     * must be a .java file.  It cannot be a directory.
     * 
     * @param javaSourceFile
     * @throws IOException 
     */
    private void generateMirahMirror(File javaSourceFile) throws IOException {
        JavaCompiler compiler = JavacTool.create();//ToolProvider.getSystemJavaCompiler();
        MyFileObject[] fos = new MyFileObject[]{new MyFileObject(javaSourceFile)};
        
        JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, Arrays.asList(fos));
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
            
            TypeFuture getType(String type){
                if ( type.endsWith("[]")){
                    // it's an array
                    String baseType = type.substring(0, type.lastIndexOf("["));
                    TypeFuture baseTypeFuture = getType(baseType);
                    
                    return typeSystem.getArrayType(baseTypeFuture);
                } else {
                    TypeFuture tf = null;
                
                    if ( internalScope != null ){
                        tf = typeSystem.loadWithScope(internalScope, type, null);
                    }
                    
                    if ( tf == null || !tf.isResolved() ){
                        return typeSystem.loadWithScope(scope, type, null);
                    } else {
                        return tf;
                    }
                }
            }

            @Override
            public Object visitCompilationUnit(CompilationUnitTree cut, Object p) {
                
                currPackage = cut.getPackageName().toString();
                namespaceStack.clear();
                namespaceStack.push(currPackage);
                scope.package_set(currPackage);
                typeSystem.addDefaultImports(scope);
                return super.visitCompilationUnit(cut, p); //To change body of generated methods, choose Tools | Templates.

            }

            @Override
            public Object visitImport(ImportTree it, Object p) {
                String path = it.getQualifiedIdentifier().toString();
                String shortName = path;
                int pos = -1;
                if ((pos = shortName.indexOf(".")) != -1) {
                    shortName = shortName.substring(pos + 1);
                }
                
                scope.add_import(path, shortName);
                return super.visitImport(it, p); //To change body of generated methods, choose Tools | Templates.
            }

            
            
            @Override
            public Object visitClass(ClassTree ct, Object p) {  
                Set<String> generics = new HashSet<String>();
                typeParams.push(generics);
                for (TypeParameterTree tree : ct.getTypeParameters()) {
                    generics.add(tree.toString());
                }
                if (ct.getSimpleName() == null || "".equals(ct.getSimpleName())) {
                    return ct;
                }
                String fullName = ct.getSimpleName().toString();
                
                fullName = getCurrentNamespacePrefix()+fullName;
                
                String superClass = "";
                TypeFuture superClassType = null;
                if (ct.getExtendsClause() != null) {
                    superClass = ct.getExtendsClause().toString();
                    superClassType = getType(formatType(superClass));
                }
                String impl = ct.getImplementsClause().toString();
                ArrayList<TypeFuture> implTypes = new ArrayList<TypeFuture>();
                if (!"".equals(impl)) {
                    String[] interfaces = impl.split(",");
                    for (String iface : interfaces) {
                        iface = iface.trim();                        
                        TypeFuture ifaceType = getType(formatType(iface));
                        implTypes.add(ifaceType);
                    }
                }
                MirahMirror mirror = null;
                //String localName = ct.getSimpleName().toString();
                //if ( namespaceStack.size() > 1 ){
                //    localName = "JavaClass$"+localName;
                //}
                
                TypeFuture newType = typeSystem.defineType(internalScope==null?scope:internalScope, null, ct.getSimpleName().toString(), superClassType, implTypes);
                if (newType instanceof MirrorFuture) {
                    MirrorFuture mf = (MirrorFuture) newType;
                    ResolvedType rt = mf.inferredType();
                    if (rt instanceof MirrorProxy) {
                        MirrorProxy mp = (MirrorProxy) rt;
                        MirrorType mt = mp.target(); // <----- AND HERE IT IS... Load this sucker up.
                        if (mt instanceof MirahMirror) {
                            mirror = (MirahMirror) mt;
                        }
                    }
                }
                if (mirror != null) {
                    int flags = getFlags(ct.getModifiers().getFlags());
                    switch (ct.getKind()) {
                        case INTERFACE:
                            flags |= Opcodes.ACC_INTERFACE;
                            break;
                            
                        case ENUM:
                            flags |= Opcodes.ACC_ENUM;
                            break;
                        
                            
                        
                        
                    }
                    mirror.flags_set(flags);
                    mirrorStack.push(mirror);
                    JVMScope oldInternal = internalScope;
                    internalScope = new JVMScope(scoper);
                    
                    
                    internalScope.selfType_set(newType);
                    //scope.addChild(classScope);
                    internalScope.parent_set(scope);
                    
                    //scope = classScope;
                    
                    namespaceStack.push(ct.getSimpleName().toString());
                    
                    Object out = super.visitClass(ct, p);
                    namespaceStack.pop();
                    
                    internalScope = oldInternal;

                    mirrorStack.pop();
                    
                    return out;
                } else {
                    return ct;
                }
            }

            /**
             * Converts modifier flags from Javac Tree into int flags usable in TypeMirror
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
                final MirahMirror mirror = mirrorStack.peek();
                String typeStr = vt.getType().toString();
                TypeFuture type = getType(formatType(typeStr));
                MemberKind kind = vt.getModifiers().getFlags().contains(Modifier.STATIC) ? MemberKind.STATIC_FIELD_ACCESS : MemberKind.FIELD_ACCESS;
                int flags = getFlags(vt.getModifiers().getFlags());
                Member member = new AsyncMember(flags, mirror, vt.getName().toString(), new ArrayList(), type, kind);
                mirror.declareField(member);
                return super.visitVariable(vt, p); //To change body of generated methods, choose Tools | Templates.
            }
            Object visitConstructor(final MethodTree mt, Object p) {
                final MirahMirror mirror = mirrorStack.peek();
                int flags = getFlags(mt.getModifiers().getFlags());
                List<TypeFuture> argTypes = new ArrayList<TypeFuture>();
                for (VariableTree v : mt.getParameters()) {                    
                    String type = formatType(v.getType().toString());
                    String pname = v.getName().toString();
                    TypeFuture tf = getType(type);
                    argTypes.add(tf);
                }
                String returnType = null;
                TypeFuture returnTypeFuture = null;
                if ( mt.getReturnType() == null ){
                    returnType = mirror.type().getClassName();
                    
                } else {
                    returnType = mt.getReturnType().toString();
                    
                }
                returnTypeFuture = getType(formatType(returnType));
                //String returnType = mt.getReturnType().toString();
                //TypeFuture returnTypeFuture = getType(formatType(returnType));
                MemberKind kind = MemberKind.CONSTRUCTOR;                
                Member member = new AsyncMember(flags, mirror, mt.getName().toString(), argTypes, returnTypeFuture, kind);
                mirror.declareField(member);
                return mt;
            }

            @Override
            public Object visitMethod(final MethodTree mt, Object p) {
                if (mt.getReturnType() == null) {
                    // It's a constructor
                    return visitConstructor(mt, p);
                }
                final MirahMirror mirror = mirrorStack.peek();
                
                int flags = getFlags(mt.getModifiers().getFlags());
                List<TypeFuture> argTypes = new ArrayList<TypeFuture>();
                for (VariableTree v : mt.getParameters()) {
                    String type = formatType(v.getType().toString());
                    String pname = v.getName().toString();
                    TypeFuture tf = getType(type);
                    AssignableTypeFuture atf = new AssignableTypeFuture(null);
                    atf.assign(tf, null);
                    argTypes.add(atf);
                }
                
                String returnType = mt.getReturnType().toString();
                TypeFuture returnTypeFuture = getType(formatType(returnType));
                MemberKind kind = mt.getModifiers().getFlags().contains(Modifier.STATIC) ? MemberKind.STATIC_METHOD : MemberKind.METHOD;
                
                
                
                MirahMethod member = new MirahMethod(mirahContext, null, flags, mirror, mt.getName().toString(), argTypes, returnTypeFuture, kind);
                mirror.add(member);
                return mt;
            }
        };
        scanner.scan(asts, null);

    }
}
