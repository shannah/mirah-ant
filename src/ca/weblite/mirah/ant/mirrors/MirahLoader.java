/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.IOException;

/**
 *
 * @author shannah
 */
public class MirahLoader extends ClassIndex.Loader {

    private boolean loaded;
    
    public MirahLoader(LoaderType type, ClassIndex.Loader parent) {
        super(type, parent);
        this.addMimetype("text/x-mirah");
    }

    @Override
    protected ClassIndex createClassIndex() {
        ClassIndex out = new ClassIndex();
        out.addIndexer(new MirahFileIndexer());
        return out;
    }

    
    
    @Override
    protected void addImportImpl(String pattern) throws IOException {
        if ( !loaded ){
            loaded = true;
            super.addImportImpl(pattern); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    public void clearCache(){
        loaded = false;
    }
    
    
    
}
