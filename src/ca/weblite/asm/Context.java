
package ca.weblite.asm;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class Context {
    Map<Class,Object> context = new HashMap<>();
    
    public<T extends Object> T get(Class<T> type){
        return (T)context.get(type);
    }
    
    public<T extends Object> void set(Class<T> type,  T obj){
        context.put(type, obj);
    }
}
