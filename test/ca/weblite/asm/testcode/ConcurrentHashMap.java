/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.asm.testcode;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
{
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        throw new RuntimeException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}