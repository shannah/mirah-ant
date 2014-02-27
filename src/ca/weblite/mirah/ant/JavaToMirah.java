/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.mirah.ant;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.jruby.org.objectweb.asm.Type;
import org.mirah.jvm.mirrors.MirahMirror;
import org.mirah.typer.FuturePrinter;
import org.mirah.typer.ResolvedType;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.TypeListener;

/**
 *
 * @author shannah
 */
public class JavaToMirah {

    private int indent = 0;
    private int tab = 4;
    private boolean useCache = false;

    static class Filewalker {
        protected void visitFile(File f){
            
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
                    //System.out.println("Dir:" + f.getAbsoluteFile());
                } else {
                    visitFile(f);
                    //System.out.println("File:" + f.getAbsoluteFile());
                }
            }
        }

    }

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

        /*
         public CharSequence getCharContent(boolean ignoreEncodingErrors) {
         return "package ca.weblite.tests;"
         + "import java.util.*;\n"
         + "public class Test extends java.util.ArrayList implements List{\n"
         + "    public Test(){"
         + "    }"
         + "    public Test(String str){"
         + "    }"
         + "    public static class InternalClassStat {}"
         + "    public static void aStaticMethod(){}"
         + "    void foobar(String bar) {\n"
         + "        Iterator<Number> itr = null;\n"
         + "        String str = itr.next();\n"
         + "        FooBar fooBar = FooBar.foobar();\n"
         + "    }\n"
         + "}";
         }
         */
    }

    
    public void createMirahStubs(File srcRoot, File mirahStubFile) throws IOException {
        final List<File> javaSourceFiles = new ArrayList<File>();
        Filewalker walker = new Filewalker(){

            @Override
            protected void visitFile(File f) {
                if ( f.getName().endsWith(".java")){
                    javaSourceFiles.add(f);
                }
            }
            
        };
        walker.walk(srcRoot.getPath());
        createMirahStubs(javaSourceFiles.toArray(new File[0]), mirahStubFile);
    }
    
    public void createMirahStubs(File[] javaSourceFiles, File mirahStubFile) throws IOException {
        if (useCache && !isChanged(javaSourceFiles, mirahStubFile)) {
            // No changes since the last generation.
            return;
        }
        if (!mirahStubFile.exists()) {
            if (!mirahStubFile.createNewFile()) {
                throw new IOException("Failed to create mirah stub file at " + mirahStubFile.getAbsolutePath());
            }

        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(mirahStubFile);

            for (File src : javaSourceFiles) {
                String mirahSrc = generateMirahStub(src);
                fw.append(mirahSrc);
            }

        } finally {
            try {
                fw.close();
            } catch (Exception ex) {
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

    public String generateMirahStub(File javaSourceFile) throws IOException {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyFileObject[] fos = new MyFileObject[]{new MyFileObject(javaSourceFile)};
        JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, Arrays.asList(fos));
        Iterable<? extends CompilationUnitTree> asts = task.parse();

        final Stack<StringBuilder> sb = new Stack<StringBuilder>();
        final Stack<Set<String>> typeParams = new Stack<Set<String>>();
        sb.push(new StringBuilder());
        //for ( CompilationUnitTree tree : asts ){
        TreePathScanner scanner;
        scanner = new TreePathScanner() {
            
            
            private boolean isGenericType(String type){
                Stack<Set> desk = new Stack<Set>();
                boolean found = false;
                while ( !typeParams.empty()){
                    Set params = typeParams.pop();
                    desk.push(params);
                    if ( params.contains(type)){
                        found = true;
                        
                        break;
                    } 
                    
                }
                while ( !desk.empty()){
                    typeParams.push(desk.pop());
                }
                return found;
            }
            
            private void nl() {
                StringBuilder s = sb.peek();
                s.append("\n");
                for (int i = 0; i < indent; i++) {
                    s.append(" ");
                }
            }

            @Override
            public Object visitLiteral(LiteralTree lt, Object p) {
                System.out.println("Literal:" + lt);
                return super.visitLiteral(lt, p); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object visitImport(ImportTree it, Object p) {
                sb.peek().append("import ").append(it.getQualifiedIdentifier().toString());
                nl();
                return it;
                //return super.visitImport(it, p); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object visitCompilationUnit(CompilationUnitTree cut, Object p) {
                sb.peek().append("package ").append(cut.getPackageName().toString());
                nl();
                return super.visitCompilationUnit(cut, p); //To change body of generated methods, choose Tools | Templates.
            }
            
            
            String formatType(String type){
                
                int pos = type.indexOf("<");
                if ( pos >= 0 ){
                    type = type.substring(0, pos);
                }
                if ( isGenericType(type)){
                    return "Object";
                }
                /*
                if ( "T".equals(type)){
                    return "Object";
                } else if ( "V".equals(type)){
                    return "Object";
                }*/
                return type;
            }

            @Override
            public Object visitClass(ClassTree ct, Object p) {
                Set<String> generics = new HashSet<String>();
                typeParams.push(generics);
                for ( TypeParameterTree tree : ct.getTypeParameters()){
                    generics.add(tree.toString());
                }
                
                if ( ct.getSimpleName() == null || "".equals(ct.getSimpleName())){
                    return ct;
                }
                sb.peek().append("class ").append(ct.getSimpleName());

                String superClass = "";
                if (ct.getExtendsClause() != null) {
                    superClass = ct.getExtendsClause().toString();
                }

                if (!"".equals(superClass)) {
                    sb.peek().append(" < ").append(formatType(superClass));
                }
                indent += tab;
                nl();

                String impl = ct.getImplementsClause().toString();
                if (!"".equals(impl)) {
                    sb.peek().append("implements ").append(impl);
                }
                nl();

                Object out = super.visitClass(ct, p); //To change body of generated methods, choose Tools | Templates.
                indent -= tab;
                nl();

                sb.peek().append("end");
                nl();
                typeParams.pop();
                return out;

            }

            Object visitConstructor(MethodTree mt, Object p) {

                sb.peek().append("def initialize");
                int len = mt.getParameters().size();
                if (len > 0) {
                    sb.peek().append("(");
                    int i = 0;
                    for (VariableTree v : mt.getParameters()) {
                        sb.peek().append(v.getName().toString()).append(":").append(formatType(v.getType().toString()));
                        if (i++ < len - 1) {
                            sb.peek().append(", ");
                        }
                    }
                    sb.peek().append(")");
                }
                nl();
                sb.peek().append("end");
                nl();

                return mt;
            }

            @Override
            public Object visitMethod(MethodTree mt, Object p) {

                if (mt.getReturnType() == null) {
                    // It's a constructor
                    return visitConstructor(mt, p);
                }

                String self = "";
                if (mt.getModifiers().getFlags().contains(Modifier.STATIC)) {
                    self = "self.";
                }

                sb.peek().append("def ").append(self).append(mt.getName().toString());
                int len = mt.getParameters().size();
                if (len > 0) {
                    sb.peek().append("(");
                    int i = 0;
                    for (VariableTree v : mt.getParameters()) {
                        String type = v.getType().toString();
                        
                        sb.peek().append(v.getName().toString()).append(":").append(formatType(type));
                        if (i++ < len - 1) {
                            sb.peek().append(", ");
                        }
                    }
                    sb.peek().append(")");
                }
                String returnType = mt.getReturnType().toString();

                sb.peek().append(":").append(formatType(returnType));
                indent += tab;

                nl();
                if (!"void".equals(returnType)) {
                    sb.peek().append("nil");
                }
                indent -= tab;
                nl();
                sb.peek().append("end");
                nl();
                //return super.visitMethod(mt, p); //To change body of generated methods, choose Tools | Templates.
                return mt;
            }

            private void TypeFuture() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };
        scanner.scan(asts, null);

        //}
        return sb.peek().toString();

    }
}
