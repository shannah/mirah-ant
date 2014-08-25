/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

/**
 *
 * @author shannah
 */
public class ClassScope extends Scope {
    private ClassIndex.Node node;
    public ClassScope(Scope parent, ClassIndex.Node node) {
        super(parent);
        this.node = node;
    }

    public ClassIndex.Node getClassNode(){
        return node;
    }
    
    
    @Override
    protected String resolveNameImpl(String name, boolean internal) {
        // This scope should only check for internal classes matching the name
        // It will rely on the parent scopes for all other searches.
        boolean isArray = TypeUtil.isArrayType(name);
        int arrayDimensions = isArray ? 
                TypeUtil.getArrayTypeDimension(name) : 
                0;
        if ( isArray ){
            name = TypeUtil.getArrayElementType(name);
        }
        
        try {
            getLoader().addImport(node.fullyQualifiedName()+".*");
        } catch ( Exception ex){
        }
        assert node != null;
        ClassIndex.Node internalClass = getLoader().find(
                node.fullyQualifiedName()+"."+name
        );
        
        if ( internalClass != null ){
            return internal ?
                    internalClass.arrayDescriptor(arrayDimensions) :
                    internalClass.fullyQualifiedArrayName(arrayDimensions);
        }
        
        return null;
        
    }
    
}
