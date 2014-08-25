/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;


import ca.weblite.mirah.ant.mirrors.TypeUtil;
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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaExtendedStubCompiler  {
    
     byte[] output;
     
     private Context context;
     
     
     
     
     
     public JavaExtendedStubCompiler(Context context){
         this.context = context;
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
    
    public List<Type> extractTypes(File sourceFile) throws IOException {
        JavaCompiler compiler = JavacTool.create();
        MyFileObject[] fos = new MyFileObject[]{
            new MyFileObject(sourceFile)
        };
        
        JavacTask task = (JavacTask) compiler.getTask(
                null, null, null, null, null, 
                Arrays.asList(fos)
        );
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        TreePathScanner scanner;
        final List<Type> types = new ArrayList<Type>();
        scanner = new TreePathScanner() {
            String packageName="";
            String currPath = "";
            
            
            @Override
            public Object visitClass(ClassTree ct, Object p) {
                if ( currPath.equals(packageName)){
                    currPath = currPath.length()>0 ? 
                    currPath+"/"+ct.getSimpleName().toString() :
                    ct.getSimpleName().toString();
                } else {
                    currPath = currPath+"$"+ct.getSimpleName().toString();
                }
                
                
                types.add(Type.getObjectType(currPath));
                Object out= super.visitClass(ct, p); 
                int lastSlash = currPath.lastIndexOf("/");
                int lastDollar = currPath.lastIndexOf("$");
                int lastPos = Math.max(lastSlash, lastDollar);
                currPath = lastPos != -1 ? currPath.substring(0, lastPos) : "";
                return out;
                
            }
            
            @Override
            public Object visitCompilationUnit(
                    CompilationUnitTree cut, 
                    Object p) {
                packageName = cut.getPackageName().toString();
                currPath = packageName.replaceAll("\\.", "/");
                return super.visitCompilationUnit(cut, p); 
            }
            
        };
        scanner.scan(asts, null);
        return types;
    }
    
    public void compileFile(File sourceFile, File destinationDirectory) 
            throws IOException {
        Map<String,byte[]> result = compile((List)null, sourceFile);
        for ( Map.Entry<String,byte[]> e : result.entrySet()){
            File output = new File(destinationDirectory, e.getKey()+".class");
            output.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(output)){
                fos.write(e.getValue());
            }
        }
    }
    
    public void compileDirectory(
            File sourceDirectory,
            File destinationDirectory,
            boolean recursive) 
            throws IOException{
        try (DirectoryStream<Path> ds = 
                Files.newDirectoryStream(sourceDirectory.toPath())){
            for ( Path p : ds){
                //System.out.println("Path "+p);
                if ( recursive && p.toFile().isDirectory() ){
                    compileDirectory(
                            p.toFile(), 
                            destinationDirectory, 
                            recursive
                    );
                } else if ( p.toFile().getName().endsWith(".java")){
                    //System.out.println("Compiling file "+p);
                    compileFile(p.toFile(), destinationDirectory);
                }
            }
        } 
    }

    private class ClassInfo {
        int numConstructors=0;
        
    }
    
    public Map<String,byte[]> compile(List<Type> types, File sourceFile) 
            throws IOException {
        //System.out.println("Compiling "+types+" in file "+sourceFile);
        final Map<String,byte[]> outMap  = new HashMap<>();
        final Set<String> typeNames = (types == null) ? 
                null : 
                new HashSet<String>();
        if ( types != null ){
            for ( Type type : types ){
                typeNames.add(type.getInternalName());
            }
        }
        JavaCompiler compiler = JavacTool.create();
        MyFileObject[] fos = new MyFileObject[]{
            new MyFileObject(sourceFile)
        };
        
        JavacTask task = (JavacTask) compiler.getTask(
                null, null, null, null, null, 
                Arrays.asList(fos)
        );
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        TreePathScanner scanner;
        
        final LinkedList<ClassFinder> scopeStack = new LinkedList<>();
        scanner = new TreePathScanner() {

            String packageName;
            ClassNode superClass;
            LinkedList<String> stack = new LinkedList<>();
            LinkedList<ClassInfo> classInfoStack = new LinkedList<>();
            LinkedList<ClassWriter> cwStack = new LinkedList<>();
            
            @Override
            public Object visitCompilationUnit(
                    CompilationUnitTree cut, 
                    Object p) {
                
                packageName = cut.getPackageName().toString();
                ClassFinder scope = new ClassFinder(
                        context.get(ClassLoader.class),
                        null
                );
                scopeStack.push(scope);
                scope.addImport(packageName+".*");
                return super.visitCompilationUnit(cut, p); 
            }

            private String getThisInternalName(String simpleName){
                simpleName = simpleName.replaceAll("\\.","$");
                StringBuilder sb = new StringBuilder();
                Iterator<String> it = stack.descendingIterator();
                sb.append(packageName.replaceAll("\\.", "/"));
                sb.append("/");

                while ( it.hasNext()){
                    sb.append(it.next()).
                            append("$");
                }
                sb.append(simpleName);
                return sb.toString();
            }
            
            

            @Override
            public Object visitImport(ImportTree it, Object p) {
                if ( !it.isStatic()){
                    String path = it.getQualifiedIdentifier().toString();
                    scopeStack.peek().addImport(path);
                }
                return super.visitImport(it, p); 
            }

            
            Object visitConstructor(final MethodTree mt, Object p) {
                ClassWriter classWriter = cwStack.peek();
                List<Type> argTypes = new ArrayList<Type>();
                for (VariableTree v : mt.getParameters()) {  

                    String type = v.getType().toString();
                    int dim = 0;
                    if ( TypeUtil.isArrayType(type)){
                        dim = TypeUtil.getArrayTypeDimension(type);
                        type = TypeUtil.getArrayElementType(type); 
                    }
                    if ( TypeUtil.isPrimitiveType(type)){
                        String descriptor = TypeUtil.getDescriptor(type);
                        argTypes.add(Type.getType(
                                TypeUtil.getArrayDescriptor(descriptor, dim)
                        ));
                    } else {
                        ClassNode stub = scopeStack.peek().findStub(type);
                        assert stub!=null;
                        argTypes.add(
                                Type.getObjectType(stub.name)
                        );
                    }

                }

                String methodDescriptor = null;
                if ( argTypes.isEmpty()){
                    methodDescriptor = 
                            Type.getMethodDescriptor(Type.getType("V"));
                } else {
                    methodDescriptor = 
                            Type.getMethodDescriptor(
                                    null,
                                    argTypes.toArray(new Type[0])
                            );
                }


                classWriter.visitMethod(
                        getFlags(mt.getModifiers().getFlags()),
                        mt.getName().toString(),
                        methodDescriptor,
                        null,
                        null
                );
                classInfoStack.peek().numConstructors++;
                return null;
            }

            
            @Override
            public Object visitMethod(MethodTree mt, Object p) {
                if (mt.getReturnType() == null) {
                    // It's a constructor
                    return visitConstructor(mt, p);
                } else {
                    ClassWriter classWriter = cwStack.peek();
                    

                    List<Type> argTypes = new ArrayList<>();
                    for (VariableTree v : mt.getParameters()) {  

                        String type = v.getType().toString();
                        int dim = 0;
                        if ( TypeUtil.isArrayType(type)){
                            dim = TypeUtil.getArrayTypeDimension(type);
                            type = TypeUtil.getArrayElementType(type); 
                        }
                        if ( TypeUtil.isPrimitiveType(type)){
                            String descriptor = 
                                    TypeUtil.getDescriptor(type);
                            argTypes.add(Type.getType(
                                    TypeUtil.getArrayDescriptor(
                                            descriptor, 
                                            dim
                                    )
                            ));
                        } else {
                            ClassNode stub = scopeStack.peek().
                                    findStub(type);
                            if ( stub == null ){
                                throw new RuntimeException(
                                        "Could not find class for "+type
                                );
                            }
                            argTypes.add(
                                    Type.getObjectType(stub.name)
                            );
                        }

                    }

                    String returnType = mt.getReturnType().toString();
                    int dim = 0;

                    Type returnTypeType = null;
                    if ( TypeUtil.isArrayType(returnType)){
                        dim = TypeUtil.getArrayTypeDimension(returnType);
                        returnType = TypeUtil.getArrayElementType(returnType); 

                    }
                    if ( TypeUtil.isPrimitiveType(returnType)){
                        String descriptor = 
                                TypeUtil.getDescriptor(returnType);
                        returnTypeType = Type.getType(
                                TypeUtil.getArrayDescriptor(
                                        descriptor, 
                                        dim
                                )
                        );


                    } else {
                        ClassNode stub = scopeStack.peek().
                                findStub(returnType);
                        if ( stub == null ){
                            throw new RuntimeException(
                                    "Could not find class for "+returnType
                            );
                        }
                        
                        returnTypeType = Type.getObjectType(stub.name);

                    }



                    String methodDescriptor = null;
                    if ( argTypes.isEmpty()){
                        methodDescriptor = 
                                Type.getMethodDescriptor(returnTypeType);
                    } else {
                        methodDescriptor = 
                                Type.getMethodDescriptor(
                                        returnTypeType,
                                        argTypes.toArray(new Type[0])
                                );
                    }

                    classWriter.visitMethod(
                            getFlags(mt.getModifiers().getFlags()),
                            mt.getName().toString(),
                            methodDescriptor,
                            null,
                            null
                    );
                    
                    
                    
                }
                return super.visitMethod(mt, p); 
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
            public Object visitClass(ClassTree ct, Object p) {
                String simpleName = ct.getSimpleName().toString();
                String internalName = getThisInternalName(simpleName);
                int lastDollar = internalName.lastIndexOf("$");
                String externalName = lastDollar==-1 ? 
                        null : 
                        internalName.substring(0, lastDollar);
                String supername = "java.lang.Object";
                String[] interfaces = null;
                boolean targetClass = false;
                if (!cwStack.isEmpty()){
                    cwStack.peek().visitInnerClass(
                            internalName, 
                            externalName,
                            simpleName, 
                            getFlags(ct.getModifiers().getFlags())
                    );
                }

                targetClass = true;
                // This is the one that we'
                //String supername = "java.lang.Object";
                if ( ct.getExtendsClause() != null ){
                    supername = ct.getExtendsClause().toString().trim();
                }
                int bracketPos = supername.indexOf("<");
                supername = bracketPos==-1 ?
                        supername:
                        supername.substring(0, bracketPos);
                ClassNode node = scopeStack.peek().findStub(supername);
                if ( node == null ){
                    throw new RuntimeException(
                            "Could not find super stub "+supername
                    );
                }
                supername = node.name;

                String impl = ct.getImplementsClause().toString();

                if (!"".equals(impl)) {
                    interfaces = impl.split(",");
                    for ( int i=0; i<interfaces.length; i++){
                        String iface = interfaces[i];
                        iface = iface.trim();                        
                        ClassNode inode = scopeStack.peek().findStub(iface);
                        assert inode != null;
                        interfaces[i] = inode.name;
                    }
                }

                int flags = getFlags(ct.getModifiers().getFlags());
                switch (ct.getKind()) {
                    case INTERFACE:
                        flags |= Opcodes.ACC_INTERFACE;
                        break;

                    case ENUM:
                        flags |= Opcodes.ACC_ENUM;
                        break;

                }

                ClassWriter classWriter = new ClassWriter(1);
                classWriter.visit(
                        1,
                        flags,
                        internalName,
                        null,
                        supername,
                        interfaces

                );
                cwStack.push(classWriter);
                classInfoStack.push(new ClassInfo());
                stack.push(simpleName);
                ClassFinder scope = new ClassFinder(
                        context.get(ClassLoader.class),
                        scopeStack.peek()
                );
                scope.addImport(
                        internalName.
                                replaceAll("/",".").
                                replaceAll("\\$", ".")+".*"
                
                );
                scope.addImport(internalName.
                                replaceAll("/",".").
                                replaceAll("\\$", ".")
                );
                scope.addImport(
                        supername.
                                replaceAll("/",".").
                                replaceAll("\\$", ".")+".*"
                
                );
                scope.addImport(supername.
                                replaceAll("/",".").
                                replaceAll("\\$", ".")
                );
                
                if ( interfaces != null ){
                    for (int i=0; i<interfaces.length; i++){
                        scope.addImport(
                                interfaces[i].
                                        replaceAll("/",".").
                                        replaceAll("\\$", ".")+".*"

                        );
                        scope.addImport(interfaces[i].
                                        replaceAll("/",".").
                                        replaceAll("\\$", ".")
                        );
                    }
                }
                scopeStack.push(scope);
                Object out = super.visitClass(ct, p); 
                stack.pop();
                scopeStack.pop();
                ClassInfo classInfo = classInfoStack.pop();
                if ( classInfo.numConstructors == 0) {
                    // there are no declared constructors in this class 
                    // we need to add a default constructor.
                    cwStack.peek().visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        Type.getMethodDescriptor(Type.getType("V")),
                        null,
                        null
                    );
                    classInfo.numConstructors++;
                }
                
                
                if ( targetClass){
                    byte[] bytes = cwStack.peek().toByteArray();
                    outMap.put(internalName, bytes);
                    cwStack.pop();
                }
                return out;
            }
            
            
            
        };
        scanner.scan(asts, null);
        return outMap;
    }
    
    public byte[] compile(final Type type, File sourceFile) throws IOException{
        Map<String,byte[]> out = 
                compile(Collections.singletonList(type), sourceFile);
        return out.get(type.getInternalName());
    }
    
    public void compile() throws IOException {
        
    }
}
