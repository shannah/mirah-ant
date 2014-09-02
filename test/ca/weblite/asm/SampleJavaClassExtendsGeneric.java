/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class SampleJavaClassExtendsGeneric extends ArrayList<SampleJavaClass>{
    public List<String> methodReturningListOfStrings(){
        return new ArrayList<String>();
    }
}
