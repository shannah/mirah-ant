/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.asm.testcode;

/**
 *
 * @author shannah
 */
public class GenericReturnTypes<T> {
    public T getGenericObject() {
        return null;
    }
    
    public void setGenericObject(T obj){
        
    }
    
    public void setGenericVarargs(T... obj){
        
    }
    
    public void setGenericArray(T[] obj){
        
    }
    
    public T[] returnGenericArray() {
        return null;
    }
    
    public <V> V getMethodGenericObject(Class<V> cls) {
        return null;
    }
}
