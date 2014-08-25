/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author shannah
 */
public class JavaClassIndex extends ClassIndex {

    public JavaClassIndex(){
        addIndexer(new JavaFileIndexer());
        addIndexer(new ClassFileIndexer());
    }
    
    
    
    
}
