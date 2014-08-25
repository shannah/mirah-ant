/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.io.File;

/**
 *
 * @author shannah
 */
public class JavaExtendedStubTool {

    private String sourcePath;
    private File destinationDirectory;
    private final Context context;
    
    
    public JavaExtendedStubTool(Context ctx,
            String sourcePath,
            File destinationDirectory
            ){
        this.context = ctx;
        this.sourcePath = sourcePath;
        this.destinationDirectory = destinationDirectory;
        
    }
    
    
    
    
}
