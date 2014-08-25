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
public class MirahClassIndex extends ClassIndex {
    private boolean scanned = false;
    public MirahClassIndex(){
        addIndexer(new MirahFileIndexer());
    }

    @Override
    public void scanPath(String path, String pattern) throws IOException {
        if ( scanned ){
            return;
        }
        super.scanPath(path, pattern); 
        scanned = true;
    }

    @Override
    public void clearCache() {
        scanned = false;
        super.clearCache(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
}
