/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant.mirrors;

import java.util.Comparator;

/**
 *
 * @author shannah
 */
public class ClassNameComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        String[] p1 = o1.split("\\.");
        String[] p2 = o2.split("\\.");
        int len = p1.length;
        if ( len != p2.length ) return -1;
        
        for ( int i=0; i<len; i++){
            String s1 = p1[i];
            String s2 = p2[i];
            if ( "*".equals(s1) || "*".equals(s2) || s1.equals(s2)){
                // do nothing  this is what we want
            } else {
                return -1;
            }
        }
        return 0;
    }
    
}
