/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import ca.weblite.asm.TypeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shannah
 */
public class PackageScope extends Scope{
    
    private final String packageName;
    public List<String> imports = new ArrayList<>();
    
    private boolean defaultImportsLoaded = false;

    public PackageScope(Scope parent, String packageName) {
        super(parent);
        this.packageName = packageName;
        
        
    }
    
    
    private void loadDefaultImports(){
        if ( !defaultImportsLoaded ){
            defaultImportsLoaded = true;
            addImport(packageName+".*");
            addImport("java.lang.*");
            addImport("*");
        }
    }
    
    public void addImport(String str) {
        imports.add(str);
        try {
            getLoader().addImport(str);
        } catch (IOException ex) {
            Logger.getLogger(PackageScope.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public String getPackageName(){
        return packageName;
    }

    
    
    
    @Override
    protected String resolveNameImpl(
            String name, 
            boolean internalRepresentation) {
        boolean isArray = TypeUtil.isArrayType(name);
        int arrayDimensions = isArray ? 
                TypeUtil.getArrayTypeDimension(name) : 
                0;
        if ( isArray ){
            name = TypeUtil.getArrayElementType(name);
        }
        loadDefaultImports();
        if ( TypeUtil.isPrimitiveType(name)){
            return internalRepresentation ?
                    TypeUtil.getArrayDescriptor(name, arrayDimensions) :
                    TypeUtil.getArrayType(name, arrayDimensions);
        }
        if ( !internalRepresentation && (
                    name.startsWith("java.") || 
                    name.startsWith("javax.") || 
                    name.startsWith("com.")
                    )
                ){
            
            return isArray ? 
                    TypeUtil.getArrayType(name, arrayDimensions) : 
                    name;
        }
        String baseName = name;
        int pos;
        if ( (pos = name.indexOf(".")) != -1 ){
            baseName = name.substring(0, pos);
        }
        
        // Check for exact
        for ( String i : imports ){
            String iName = i;
            String prefix = "";
            if ( (pos = i.lastIndexOf(".")) != -1  ){
                iName = i.substring(pos+1);
                prefix = i.substring(0,pos);
            }
            if ( iName.equals(baseName)){
                String fqn = prefix + "." + name;
                if ( internalRepresentation ){
                    ClassIndex.Node node = getLoader().find(fqn);
                    if ( node != null ){
                        return node.arrayDescriptor(arrayDimensions);
                    } else {
                        try {
                            getLoader().addImport(prefix+".*");
                            node = getLoader().find(fqn);
                            if ( node == null ){
                                //throw new ClassNotFoundException(
                                //        fqn+" could not be found"
                                //);
                            } else {
                                return node.arrayDescriptor(arrayDimensions);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    return TypeUtil.getArrayType(fqn, arrayDimensions);
                }
            }
        }
        
        // Now check for globs 
        List<String> toCheck = new ArrayList<String>();
        toCheck.add(packageName+".*");
        toCheck.add("java.lang.*");
        toCheck.addAll(imports);
        toCheck.add("*");
        for ( String i : toCheck ){
            String iName = i;
            String prefix = "";
            if ( (pos = i.lastIndexOf(".")) != -1  ){
                iName = i.substring(pos+1);
                prefix = i.substring(0,pos);
            }
            if ( "*".equals(iName)){
                try {
                    getLoader().addImport(i);
                } catch (IOException ex) {
                    Logger.getLogger(PackageScope.class.getName()).log(Level.SEVERE, null, ex);
                }
                String fqn = prefix+"."+name;
                
                if ( fqn.startsWith(".")){
                    fqn = fqn.substring(1);
                }
                ClassIndex.Node node = getLoader().find(fqn);
                if ( node != null ){
                    return internalRepresentation ?
                            node.arrayDescriptor(arrayDimensions) : 
                            node.fullyQualifiedArrayName(arrayDimensions);
                }
                
                
            }
            
        }
        
        // If we are here then we didn't find it yet.  Try an import
        String importStr = name;
        pos = -1;
        if ( (pos=importStr.lastIndexOf(".")) != -1 ){
            importStr = importStr.substring(0,pos)+".*";
        } else {
            importStr = "*";
        }
        try {
            getLoader().addImport(importStr);
        } catch (IOException ex) {
            Logger.getLogger(PackageScope.class.getName()).log(Level.SEVERE, null, ex);
        }
        ClassIndex.Node node = getLoader().find(name);
        if ( node != null ){
            return internalRepresentation ?
                    node.arrayDescriptor(arrayDimensions) :
                    node.fullyQualifiedArrayName(arrayDimensions);
        } 
        
        return null;
    }
    
    
    
}
