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
public class SampleJavaClass {
    int someField;
    Object someObjField;
    protected Object protectedField;
    private Object privateField;
    public Object publicField;
    
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
