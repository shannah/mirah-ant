/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 *
 * @author shannah
 */
public class ClassFileIndexer implements ClassIndex.Indexer{
    
    private ClassNameComparator cmp = new ClassNameComparator();
    

    @Override
    public boolean canIndex(String path, String relPath, String pattern) {
        return path.endsWith(".class") && ( pattern == null ||
                cmp.compare(
                        relPath.
                                replaceAll("\\.class$", "").
                                replaceAll("/", ".").
                                replaceAll("\\$","."), 
                        pattern
                ) == 0
                );
            
        
    }

    @Override
    public void index(String path, 
            String relPath, 
            InputStream contents, 
            ClassIndex index, 
            String pattern) 
            throws IOException {
        
        if ( relPath.startsWith("/")){
            relPath = relPath.substring(1);
        }
        String internalName = relPath.replaceAll("\\.class$", "");
        String cpath = internalName.replaceAll("/", ".").replaceAll("\\$", ".");
        index.addClass(cpath, internalName);
        
    }

    @Override
    public boolean indexingRequiresFileContents(String path, String relPath, String pattern) {
        return false;
    }
    
}
