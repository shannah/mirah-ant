/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class ClassFinder {
    
    final ClassFinder parent;
    final ClassLoader loader;
    ArrayList<SearchPath> searchPaths = new ArrayList<>();
    
    /**
     * Maps simple name to full name
     */
    Map<String,String> vars = new HashMap<>();
    Map<String,ClassNode> classCache = new HashMap<>();
    Map<String,ClassNode> stubCache = new HashMap<>();
    Set<String> typeParameters = new HashSet<>();
    
    private class SearchPath {
        String path;
        int order;
        
        static final int ORDER_CLASS=0;
        static final int ORDER_INTERNAL_CLASSES=1;
        static final int ORDER_PACKAGE=2;

        @Override
        public String toString() {
            return path;
        }
        
        
    }
    
    
    
    private SearchPath searchPath(String path, int order){
        SearchPath p = new SearchPath();
        p.path = path;
        p.order = order;
        return p;
    }
    
    public ClassFinder(ClassLoader loader, ClassFinder parent){
        this.loader = loader;
        this.parent = parent;
        this.addDefaultPackage();
        this.addImport("java.lang.*");
    }
    
    public void addImport(String importStmt){
        int lastDot = importStmt.lastIndexOf(".");
        String prefix = lastDot==-1 ? 
                importStmt : 
                importStmt.substring(0, lastDot);
        String lastPart = lastDot==-1 ?
                importStmt :
                importStmt.substring(lastDot+1);
        if ( "*".equals(lastPart)){
            searchPaths.add(searchPath(importStmt,0));
        } else {
            vars.put(lastPart, importStmt);
        }
        
    }
    
 
    
    
    
    public void addDefaultPackage(){
        searchPaths.add(searchPath("*", 0));
    }
    
    private ClassNode findFQNClass(String name){
        if ( name.indexOf(".") != -1 ){
            name = name.replaceAll("\\.", "/");
        }
        String origName = name;
        if ( classCache.containsKey(name)){
            return classCache.get(name);
        }
        
        while ( loader != null &&  true ){
            Type type = Type.getObjectType(name);
            ClassNode node = loader.findClass(Type.getObjectType(name));
            if ( node != null ){
                classCache.put(origName, node);
                return node;
            }
            int lastSlash = name.lastIndexOf("/");
            if ( lastSlash == -1 ){
                break;
            }
            name = name.substring(0, lastSlash)+"$"+name.substring(lastSlash+1);
        }
        
        return null;
            
    }
    
    private ClassNode findFQNStub(String name){
        if ( name.indexOf(".") != -1 ){
            name = name.replaceAll("\\.", "/");
        }
        String origName = name;
        if ( stubCache.containsKey(name)){
            return stubCache.get(name);
        }
        
        while ( loader != null &&  true ){
            ClassNode node = loader.findStub(Type.getObjectType(name));
            if ( node != null ){
                stubCache.put(origName, node);
                return node;
            }
            int lastSlash = name.lastIndexOf("/");
            if ( lastSlash == -1 ){
                break;
            }
            name = name.substring(0, lastSlash)+"$"+name.substring(lastSlash+1);
        }
        return null;
            
    }
    
    public boolean isTypeParameter(String name){
        return typeParameters.contains(name);
    }
    
    public void addTypeParameter(String type){
        typeParameters.add(type);
    }
    
    public void removeTypeParameter(String type){
        typeParameters.remove(type);
    }
    
    public Set<String> getTypeParameters(){
        return Collections.unmodifiableSet(typeParameters);
    }
    
    public void clearTypeParameters(){
        typeParameters.clear();
    }
    
    
    
    public ClassNode findClass(String name){
        if ( classCache.containsKey(name)){
            return classCache.get(name);
        }
        int firstDot = name.indexOf(".");
        String firstPart = firstDot==-1 ? name : name.substring(0,firstDot);
        if ( vars.containsKey(firstPart)){
            String remaining = firstDot==-1 ? "" : 
                    "."+name.substring(firstDot+1);
            ClassNode out = findFQNClass(vars.get(firstPart)+remaining);
            if ( out != null ){
                classCache.put(name, out);
                return out;
            }
        }
        for ( SearchPath path : searchPaths ){
            String resolvedPath = path.path.replace("*", name);
            ClassNode out = findFQNClass(resolvedPath);
            if ( out != null ){
                classCache.put(name, out);
                return out;
            }
            
        }
        if ( parent != null ){
            return parent.findClass(name);
        }
        return null;
    }
    
    public ClassNode findStub(String name){
        if ( stubCache.containsKey(name)){
            return stubCache.get(name);
        }
        int firstDot = name.indexOf(".");
        String firstPart = firstDot==-1 ? name : name.substring(0,firstDot);
        if ( vars.containsKey(firstPart)){
            String remaining = firstDot==-1 ? "" : 
                    "."+name.substring(firstDot+1);
            ClassNode out = findFQNStub(vars.get(firstPart)+remaining);
            if ( out != null ){
                stubCache.put(name, out);
                return out;
            }
        }
        
        for ( SearchPath path : searchPaths ){
            String resolvedPath = path.path.replace("*", name);
            ClassNode out = findFQNStub(resolvedPath);
            if ( out != null ){
                stubCache.put(name, out);
                return out;
            }
            
        }
        if ( parent != null ){
            return parent.findStub(name);
        }
        return null;
    }
    
    
}
