/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import ca.weblite.mirah.ant.mirrors.ClassIndex.Node;
import ca.weblite.mirah.ant.mirrors.ClassIndex.NodeType;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTool;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 *
 * @author shannah
 */
public class JavaFileIndexer implements ClassIndex.Indexer {

    @Override
    public boolean indexingRequiresFileContents(String path, String relPath, String pattern) {
        return true;
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
    
    
    public boolean fileMayContainClass(String relPath, String pattern){
        String baseClass = relPath.replaceAll("\\.java$", "")
                .replaceAll("/", ".");
        String[] p1 = baseClass.split("\\.");
        String[] p2 = pattern.split("\\.");
        int len = p1.length;
        if ( len > p2.length ){
            return false;
        }
        
        for ( int i=0; i<len; i++){
            String s1 = p1[i];
            String s2 = p2[i];
            if ( "*".equals(s1) || "*".equals(s2) || s2.equals(s2)){
                // do nothing... this is what we want
            } else {
                return false;
            }
        }
        return true;
        
    }
    
    
    @Override
    public boolean canIndex(String path, String relPath, String pattern) {
        return path.endsWith(".java") && (pattern == null ||fileMayContainClass(relPath, pattern));
    }

    
    
    @Override
    public void index(String path, String relPath, InputStream contents, final ClassIndex index, String pattern) throws IOException {
        
        // Create a temp fiel with the inputstream
        File tempFile = File.createTempFile("tempfile", "java");
        try {
            tempFile.delete();
            Files.copy(contents, tempFile.toPath());


            JavaCompiler compiler = JavacTool.create();//ToolProvider.getSystemJavaCompiler();
            MyFileObject[] fos = new MyFileObject[]{new MyFileObject(tempFile)};

            JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, Arrays.asList(fos));
            Iterable<? extends CompilationUnitTree> asts = task.parse();
            final LinkedList<ClassIndex.Node> stack = new LinkedList<>();
            TreePathScanner scanner;
            scanner = new TreePathScanner() {

                String packageName=null;

                @Override
                public Object visitCompilationUnit(CompilationUnitTree cut, Object p) {
                    packageName = cut.getPackageName().toString();
                    return super.visitCompilationUnit(cut, p); //To change body of generated methods, choose Tools | Templates.
                }



                @Override
                public Object visitClass(ClassTree ct, Object p) {
                    String simpleName  = ct.getSimpleName().toString();
                    String internalName = simpleName;

                    if ( stack.isEmpty() ){
                        if (packageName != null ){
                            internalName = packageName.replaceAll("\\.", "/")+"/"+simpleName;
                        }

                    } else {
                        internalName = stack.peek().internalName+"$"+simpleName;
                    }
                    String path = internalName.replaceAll("/", ".").replaceAll("\\$", ".");
                    Node cls = index.addClass(path, internalName);
                    stack.push(cls);
                    Object out = super.visitClass(ct, p); 
                    stack.pop();
                    return out;

                }

            };
            scanner.scan(asts, null);
        } finally {
            tempFile.delete();
        }
    }
    
}
