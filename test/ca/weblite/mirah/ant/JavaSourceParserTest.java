/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;



import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.junit.Test;

/**
 *
 * @author shannah
 */
public class JavaSourceParserTest {
     Trees trees;
     JavacTask task = null;
     class MyFileObject extends SimpleJavaFileObject {
        public MyFileObject() {
            super(URI.create("myfo:/Test.java"), JavaFileObject.Kind.SOURCE);
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return "import java.util.*;\n"
                + "public class Test {\n"
                + "    void foobar() {\n"
                + "        Iterator<Number> itr = null;\n"
                + "        String str = itr.next();\n"
                + "        FooBar fooBar = FooBar.foobar();\n"
                + "    }\n"
                + "}";
        }
    }
    
    public JavaSourceParserTest() {
    }
    
    
    
    
    class MyVisitor extends TreePathScanner<Void,Void> {
        public boolean foundError = false;
        CompilationUnitTree compilationUnit = null;
        int i = 0;
        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void ignored) {
            TreePath path = TreePath.getPath(compilationUnit, node);
            TypeMirror typeMirror = trees.getTypeMirror(path);
            if (typeMirror.getKind() == TypeKind.ERROR) {
              if (i == 0) {
                String str1 = trees.getOriginalType((ErrorType)typeMirror).toString();
                if (!str1.equals("java.lang.Number")) {
                    throw new AssertionError("Trees.getOriginalType() error!");
                }

                Types types = task.getTypes();

                str1 = types.asElement(trees.getOriginalType((ErrorType)typeMirror)).toString();
                if (!str1.equals("java.lang.Number")) {
                    throw new AssertionError("Types.asElement() error!");
                }

                i++;
              }
              else if (i == 1) {
                String str1 = trees.getOriginalType((ErrorType)typeMirror).toString();
                if (!str1.equals("FooBar")) {
                    throw new AssertionError("Trees.getOriginalType() error!");
                }

                Types types = task.getTypes();

                if (types.asElement(trees.getOriginalType((ErrorType)typeMirror)) != null) {
                    throw new AssertionError("Ttypes.asElement() error!");
                }
                foundError = true;
              }
            }


            return null;
        }

    }

    @Test
    public void testSomeMethod() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MyFileObject[] fos = new MyFileObject[]{new MyFileObject()};
        task = (JavacTask) compiler.getTask(null, null, null, null, null, Arrays.asList(fos));
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        task.analyze();
        trees = Trees.instance(task);
        MyVisitor myVisitor = new MyVisitor();
        for (CompilationUnitTree ast : asts) {
            myVisitor.compilationUnit = ast;
            myVisitor.scan(ast, null);
        }

        if (!myVisitor.foundError) {
            throw new AssertionError("Expected error not found!");
        }

               
    }
    
    
    
}
