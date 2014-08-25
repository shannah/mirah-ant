/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.mirah.jvm.mirrors.ArrayType;
import org.mirah.jvm.mirrors.MirrorLoader;
import org.mirah.jvm.mirrors.MirrorType;
import org.mirah.jvm.mirrors.ResourceLoader;
import org.mirah.jvm.mirrors.SimpleMirrorLoader;
import org.mirah.util.Context;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaSourceMirrorLoader extends SimpleMirrorLoader {
    private Context context;
    private ResourceLoader loader;
    private String classPath;
    
    public JavaSourceMirrorLoader(Context context,
            ResourceLoader resourceLoader,
            MirrorLoader parent){
        super(parent);
        this.context = context;
        this.loader = resourceLoader;
    }

    @Override
    public MirrorType findMirror(Type type) {
        MirrorType out = null;
        if (( out = super.findMirror(type)) == null){
            if ( type.getSort() == Type.ARRAY){
                return findArrayMirror(Type.getType(type.getDescriptor().substring(1)));
            }
            String path = null;
            String[] parts = type.getInternalName().split("/");
            int len = parts.length;
            while ( path == null && len > 0 ){
                StringBuilder sb = new StringBuilder();
                for ( int i=0; i<len; i++){
                    sb.append(parts[i]).append("/");
                }
                String test = sb.substring(0, sb.length()-1)+".java";
                path = findInClassPath(test);       
            }
            ClassNode node = new ClassNode();
            
        }
        return out;
    }
    
    public MirrorType findArrayMirror(Type type){
        MirrorType component = findMirror(type);
        if ( component != null ){
            return new ArrayType(context, component);
        }
        return null;
    }
    
    String findInClassPath(String file){
        String[] paths = classPath.split(Pattern.quote(File.pathSeparator));
        for ( String root : paths ){
            File f = new File(root, file);
            if ( f.exists() ){
                return f.getPath();
            }
        }
        return null;
    }
}
