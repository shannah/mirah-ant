/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import org.mirah.jvm.mirrors.ArrayType;
import org.mirah.jvm.mirrors.BytecodeMirror;
import org.mirah.jvm.mirrors.MirrorType;
import org.mirah.jvm.mirrors.OrErrorLoader;
import org.mirah.jvm.mirrors.SimpleMirrorLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class JavaSourceMirrorLoader extends SimpleMirrorLoader {
    private final org.mirah.util.Context context;
    private final JavaSourceClassLoader loader;
    private final OrErrorLoader ancestorLoader;
    
    public JavaSourceMirrorLoader(
            org.mirah.util.Context context,
            JavaSourceClassLoader resourceLoader,
            org.mirah.jvm.mirrors.MirrorLoader parent){
        super(parent);
        this.context = context;
        this.loader = resourceLoader;
        this.ancestorLoader = new OrErrorLoader(this);
    }
    
    @Override
    public MirrorType findMirror(Type type){
        MirrorType out = super.findMirror(type);
        if ( out != null ){
            return out;
        }
        
        
        
        if ( type.getSort() == Type.ARRAY ){
            return findArrayMirror(
                    Type.getType(type.getDescriptor().substring(1))
            );
        }
        
        ClassNode node = loader.findClass(type);
        if ( node != null ){
            BytecodeMirror mirror = new BytecodeMirror(context,
                    node,
                    ancestorLoader
            );
            return mirror;
        }
        return null;
        
        
    }
    
    public MirrorType findArrayMirror(Type type){
        MirrorType component = loadMirror(type);
        if ( component != null ){
            return new ArrayType(context, component);
        }
        return null;
    }
}
