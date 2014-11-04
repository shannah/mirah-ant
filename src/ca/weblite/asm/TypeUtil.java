/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.asm;

import ca.weblite.asm.ClassFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author shannah
 */
public class TypeUtil {
    static Map<String,String> primitiveDescriptors;
    
    /**
     * Map of primitive type descriptors.
     * @return 
     */
    public static Map<String,String> primitiveDescriptors(){
        if ( primitiveDescriptors == null ){
            primitiveDescriptors = new HashMap<String,String>();
            String[] d = new String[]{
                "void", "V",
                "boolean", "Z",
                "char", "C",
                "byte", "B",
                "short", "S",
                "int", "I",
                "float", "F",
                "long", "J",
                "double", "D"
            };
            for ( int i=0; i<d.length; i+=2){
                primitiveDescriptors.put(d[i], d[i+1]);
            }
        }
        return primitiveDescriptors;
    }
    
    public static boolean isPrimitiveType(String type) {
         return primitiveDescriptors().containsKey(type) || 
                 primitiveDescriptors.values().contains(type);
    }

    /**
     * Gets the descriptor for a primitive type.
     * @param type The type.  E.g. "int"
     * @return The descriptor for the type.  E.g. "I"
     */
    public static String getPrimitiveDescriptor(String type) {
        if ( primitiveDescriptors.values().contains(type)){
            return type;
        }
        return primitiveDescriptors().get(type);
    }
    
    
    public static String getMethodSignature(ClassFinder scope, String returnType, String ... argTypes){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for ( String argType : argTypes ){
            sb.append(getTypeSignature(argType, scope));
        }
        sb.append(")");
        sb.append(getTypeSignature(returnType, scope));
        return sb.toString();
    }
    
    public static String getClassSignature(ClassFinder scope, Collection<String> typeParameters, String superType, String... interfaces){
        
        ClassFinder newScope = new ClassFinder(null, scope);
        
        StringBuilder sb = new StringBuilder();
        if ( typeParameters != null && !typeParameters.isEmpty()){
            sb.append("<");
            for ( String param : typeParameters ){
                String[] parts = param.split(" extends ");
                String boundType = "java.lang.Object";
                if ( parts.length > 1 ){
                    boundType= parts[1].trim().split(" ")[0];
                }
                parts[0] = parts[0].trim();
                sb.append(parts[0]).append(":").append(getTypeSignature(boundType, scope));
                newScope.addTypeParameter(parts[0]);
                
            }
            sb.append(">");
        }
        sb.append(getTypeSignature(superType, newScope));
        if ( interfaces != null && interfaces.length > 0 ){
            for ( String iface : interfaces){
                sb.append(getTypeSignature(iface, newScope));
            }
        }
        
        return sb.toString();
    }
    
    public static String getTypeSignature(String type, ClassFinder scope){
        if ( isArrayType(type)){
            int dim = getArrayTypeDimension(type);
            String elType = getArrayElementType(type);
            String elSig = getTypeSignature(elType, scope);
            StringBuilder sb = new StringBuilder();
            for ( int i=0; i<dim; i++){
                sb.append("[");
            }
            sb.append(elSig);
            return sb.toString();
        } else if ( isPrimitiveType(type)){
            return getDescriptor(type);
        } else {
            String fullType = type;
            String[] generics=null;
           
            int lePos = type.indexOf("<");
            if ( lePos != -1 ){
                type = type.substring(0, lePos);
                String genericsStr = fullType.substring(lePos+1, fullType.lastIndexOf(">"));
                List<String> g = new ArrayList<>();
                char c;
                int pos=0;
                int l1=0;
                int l2=0;
                char[] chars = genericsStr.toCharArray();
                int len = chars.length;
                int mark=0;
                while ( pos < len ){
                    c = chars[pos];
                    switch (c){
                        case '<': l1++;break;
                        case '>': l1--;break;
                        case '[': l2++;break;
                        case ']': l2--;break;
                        case ',':
                            if ( l1==0 ){
                                g.add(genericsStr.substring(mark, pos).trim());
                                mark=pos+1;
                            }
                    }
                    
                    pos++;
                }
                
                g.add(genericsStr.substring(mark, pos).trim());
                generics = g.toArray(new String[0]);
                
            }
            StringBuilder sb = new StringBuilder();
            if ( scope.isTypeParameter(type)){
                sb.append("T").append(type);
            } else {
                ClassNode baseNode = scope.findStub(type);
                if ( baseNode == null ){
                    throw new RuntimeException("Could not find class "+type);
                }


                sb.append("L")
                        .append(baseNode.name);
            }
            if ( generics != null ){
                sb.append("<");
                for ( String g : generics ){
                    sb.append(getTypeSignature(g, scope));
                }
                sb.append(">");
            }
            sb.append(";");
            return sb.toString();
                
        }
        
    }
    
    /**
     * Gets the descriptor for a specified internal type.
     * @param internalName The internal name of the type.  E.g. java/lang/Object
     * @return The descriptor.  E.g. Ljava/lang/Object;
     */
    public static String getDescriptor(String internalName){
        if ( isPrimitiveType(internalName)){
            return getPrimitiveDescriptor(internalName);
        } else {
            return "L"+internalName+";";
        }
    }
    
    /**
     * Returns a descriptor for array type with the given element internal name
     * @param internalName The internal name of the element type.  E.g. 
     * java/lang/Object
     * @param dimensions  The number of dimensions to make the array type.
     * @return The array descriptor.  E.g. [[Ljava/lang/Object;
     */
    public static String getArrayDescriptor(String internalName, int dimensions){
        StringBuilder sb = new StringBuilder();
        for ( int i=0; i<dimensions; i++){
            sb.append("[");
        }
        sb.append(getDescriptor(internalName));
        return sb.toString();
    }
    
    /**
     * Checks to see if a given type is an array type.
     * @param type  The type in dot notation.  E.g. java.lang.Object[]
     * @return True if type is array type.
     */
    public static boolean isArrayType(String type){
        return type.endsWith("[]");
    }
    
    /**
     * Returns the number of dimensions in an array type.  E.g. Object[] =>1,
     * Object[][] => 2 etc...
     * @param type
     * @return 
     */
    public static int getArrayTypeDimension(String type){
        String parts[] = type.replaceAll("<[^>]*>", "").split("\\[");
        return parts.length-1;
    }
    
    /**
     * Converts an array type (e.g. Object[] to its element type e.g. Object)
     * @param type
     * @return 
     */
    public static String getArrayElementType(String type){
        if ( !isArrayType(type)){
            return null;
        } else {
            int lePos = type.lastIndexOf(">");
            if ( lePos<0 ) lePos = 0;
            return type.substring(0, type.indexOf("[", lePos));
        }
    }
    
    /**
     * Gets the array type for a scalar type. 
     * @param elementType The element type in dot notation. E.g. 
     * java.lang.Object
     * @param dimension The dimension of the array to create.
     * @return Array type in dot notation. e.g. java.lang.Object[]
     */
    public static String getArrayType(String elementType, int dimension){
        StringBuilder sb = new StringBuilder();
        sb.append(elementType);
        for ( int i=0; i<dimension; i++){
            sb.append("[]");
        }
        return sb.toString();
    }
    
   /**
    * Converts modifier flags from Javac Tree into int flags usable in TypeMirror
    * @param mods
    * @return 
    */
   public static int getFlags(Set<Modifier> mods) {
       int flags = 0;
       for (Modifier m : mods) {
           switch (m) {
               case ABSTRACT:
                   flags |= Opcodes.ACC_ABSTRACT;
                   break;
               case FINAL:
                   flags |= Opcodes.ACC_FINAL;
                   break;
               case PRIVATE:
                   flags |= Opcodes.ACC_PRIVATE;
                   break;
               case PROTECTED:
                   flags |= Opcodes.ACC_PROTECTED;
                   break;
               case PUBLIC:

                   flags |= Opcodes.ACC_PUBLIC;
                   break;
               case STATIC:
                   flags |= Opcodes.ACC_STATIC;
                   break; 
               
           }
       }

       return flags;
   }
    
}
