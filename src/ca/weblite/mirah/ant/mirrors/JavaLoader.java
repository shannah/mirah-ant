/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

/**
 *
 * @author shannah
 */
public class JavaLoader extends ClassIndex.Loader {

    public JavaLoader(
            LoaderType type,
            ClassIndex.Loader parent) {
        super(type, parent);
        this.addMimetype("application/x-java");
        this.addMimetype("text/x-java");
    }

    @Override
    protected ClassIndex createClassIndex() {
        ClassIndex out = new ClassIndex();
        out.addIndexer(new ClassFileIndexer());
        out.addIndexer(new JavaFileIndexer());
        return out;
        
    }

    
    
    
    
}
