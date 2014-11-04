 package ca.weblite.asm;


import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaStubFactory {

    
    
    
    Context context;
    
    //ClassNode out = null;
    
    public JavaStubFactory(Context context){
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
    
    
    public Set<ClassNode> createStubs(File file) throws IOException{
        final Set<ClassNode> returnMap = new HashSet<>();
        File tempFile = file;

        JavaCompiler compiler = JavacTool.create();
        MyFileObject[] fos = new MyFileObject[]{new MyFileObject(tempFile)};

        JavacTask task = (JavacTask) compiler.getTask(
                null, null, null, null, null, Arrays.asList(fos)
        );
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        final LinkedList<String> stack = new LinkedList<>();
        TreePathScanner scanner;
        final LinkedList<ClassFinder> scopeStack = new LinkedList<>();

        ClassFinder classFinder = 
                new ClassFinder(context.get(ClassLoader.class), null);
        scopeStack.push(classFinder);

        scanner = new TreePathScanner() {

            String packageName=null;

            @Override
            public Object visitCompilationUnit(
                    CompilationUnitTree cut, 
                    Object p) {
                packageName = cut.getPackageName().toString();
                scopeStack.peek().addImport(packageName+".*");
                return super.visitCompilationUnit(cut, p);
            }

            @Override
            public Object visitImport(ImportTree it, Object p) {
                if ( !it.isStatic()){
                    scopeStack.peek().addImport(

                            it.getQualifiedIdentifier().toString()
                    );

                }
                return super.visitImport(it, p);
            }



            private String getInternalName(String simpleName){
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
            public Object visitClass(ClassTree ct, Object p) {
                if ( scopeStack.isEmpty() ){
                    scopeStack.push(
                            new ClassFinder(
                                    context.get(ClassLoader.class), 
                                    null
                            )
                    );
                }
                String simpleName  = ct.getSimpleName().toString();
                String internalName = getInternalName(simpleName);
                //if ( type.getInternalName().equals(internalName)){
                ClassNode node = new ClassNode();
                node.name = internalName;
                node.version = 1;
                node.access = TypeUtil.getFlags(
                        ct.getModifiers().getFlags()
                );
                String supername = "java.lang.Object";
                if ( ct.getExtendsClause() != null ){
                    supername = ct.getExtendsClause().toString();
                }
                supername = supername.split(Pattern.quote("<"))[0];
                ClassNode superClass = 
                        scopeStack.peek().findStub(supername);
                if ( superClass == null ){
                    throw new RuntimeException(
                            "Could not find class "+supername
                    );
                }
                //assert superClass != null;
                node.superName = superClass.name;



                String impl = ct.getImplementsClause().toString();
                String[] interfaces = null;
                if (!"".equals(impl)) {
                    interfaces = impl.split(",");
                    for ( int i=0; i<interfaces.length; i++){
                        String iface = interfaces[i];
                        iface = iface.trim();                        
                        ClassNode inode = scopeStack.peek().findStub(iface);
                        assert inode != null;
                        interfaces[i] = inode.name;
                    }
                    node.interfaces = Arrays.asList(interfaces);
                }



                //out = node;
                returnMap.add(node);

                stack.push(simpleName);
                Object out = super.visitClass(ct, p); 
                stack.pop();
                return out;

                //} else {
                //    stack.push(simpleName);
                //    Object out = super.visitClass(ct, p); 
                //    stack.pop();
                //    return out;
                //}

            }

        };
        scanner.scan(asts, null);
        return returnMap;
       
        
    }
    
    
    
    public ClassNode createStub(final Type type, File file) 
            throws IOException {
        Set<ClassNode> stubs = createStubs(file);
        for ( ClassNode n : stubs ){
            if ( type.getInternalName().equals(n.name)){
                return n;
            }
        }
        return null;
    }
    
}
