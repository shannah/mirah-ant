/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import ca.weblite.mirah.ant.mirrors.*;
import java.util.ArrayList;

/**
 *
 * @author shannah
 */
public class SampleJavaClass extends ArrayList {
    int someField;
    Object someObjField;
    protected Object protectedField;
    private Object privateField;
    public Object publicField;
    
    
    public void someMethod(){
        
    }
    
    public void someMethodWithParams(int intParam, ArrayList listParam){
        
    }
    
    public void someMethodReferencingInternalClass(StaticInternalClass cls){
        
    }
    
    public static class StaticInternalClass {
        public static class NestedStaticInternalClass{
            
        }
    }
    
    public static enum StaticInternalEnum {
        OPTION1,
        OPTION2
        
    }
    
    public static interface StaticInternalInterface {
        
    }
    
    public class InternalClass {
        public class NestedInternalClass {
            
        }
    }
    
    private class PrivateInternalClass {
        
    }
}
