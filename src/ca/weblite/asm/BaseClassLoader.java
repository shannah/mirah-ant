/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class BaseClassLoader implements ClassLoader {

    ClassLoader parent;
    Context context;
    
    public BaseClassLoader(Context context, ClassLoader parent){
        this.parent = parent;
        this.context = context;
        context.set(ClassLoader.class, this);
    }
    
    @Override
    public ClassNode findClass(Type type) {
        if ( parent != null ){
            return parent.findClass(type);
        }
        return null;
    }

    @Override
    public ClassNode findStub(Type type) {
         if ( parent != null ){
             return parent.findStub(type);
         }
        return null;
    }
    
    public ClassLoader getParent(){
        return parent;
    }
    
    public Context getContext(){
        return context;
    }
    
}
