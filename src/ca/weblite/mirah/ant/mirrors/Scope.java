/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class Scope {

    
    private ResourceLoader loader;
    private Scope parent;
    static final String CLASS_NOT_FOUND = ".....CLASS_NOT_FOUND.....";
    public Map<String,String> resolvedNames = new HashMap<>();
    private Map<String,String> resolvedInternalNames = new HashMap<>();
    
    public Scope(Scope parent){
        
        this.parent = parent;
        
    }
    
    public final String resolveName(
            String name, 
            boolean internalRepresentation){
        Map<String,String> map = internalRepresentation ? 
                resolvedInternalNames :
                resolvedNames;
        
        String res = map.get(name);
        if ( res == null ){
            res = resolveNameImpl(name, internalRepresentation);
            if ( res == null){
                map.put(name, CLASS_NOT_FOUND);
                
            } else {
                map.put(name, res);
                
            }
            return resolveName(name, internalRepresentation);
        } else if ( CLASS_NOT_FOUND.equals(res)){
            if ( parent != null ){
                return parent.resolveName(name, internalRepresentation);
            } 
        } else {
            return res;
        }
        return null;
    }
    
    
    protected String resolveNameImpl(
            String name, 
            boolean internalRepresentation) {
        return name;
    }
    public ResourceLoader getLoader(){
        if ( loader == null && parent != null ){
            return parent.getLoader();
        }
        return loader;
    }
    
    public void setLoader(ResourceLoader loader){
        this.loader = loader;
    }
    
}
