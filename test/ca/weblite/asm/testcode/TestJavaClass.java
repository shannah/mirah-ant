/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm.testcode;

/**
 *
 * @author shannah
 */
public class TestJavaClass {
    
    public static int testInt=0;
    
    public void start(){
        new TestMirahClass().hello();
    }
    
    public static void hello2(){
        System.out.println("Hello from java");
    }
    
    public static class InternalClass {
        public static void hello3(){
            System.out.println("Hello from internal class");
        }
        
        public String getHello(){
            return "hello from internal class method";
        }
    }
}
