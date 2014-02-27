/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.mirah.ant;

import java.io.File;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner7;
import javax.tools.JavaFileObject;
import org.jruby.org.objectweb.asm.Opcodes;
import org.jruby.org.objectweb.asm.Type;
import org.mirah.util.Context;

/**
 *
 * @author shannah
 */
public class JavaSourceParser extends ElementScanner7 {
        
        private JavaFileObject file;
    
        public JavaSourceParser(JavaFileObject file){
            this.file = file;
        }
    
        Context context;
        
        
        
        
        /**
         * def visitType(elem, arg)
          return anno_visitType(elem, arg) if elem.kind_of?(TypeMirror)
          if elem.annotation_mirrors.any? {|a| a.toString == '@org.mirah.infer.FakeClass'}
            return
          end
          superclass = internal_type_name(elem.superclass) || 'java/lang/Object'
          interfaces = elem.interfaces.map {|i| internal_type_name(i)}
          flags = flags_from_modifiers(elem)
          builder = BiteScript::ASM::ClassMirror::Builder.new
          builder.visit(0, flags, internal_name(elem), nil, superclass, interfaces)
          with(builder) do
            elem.annotation_mirrors.each {|anno| visitAnnotation(anno)}
            super(elem, arg)
          end
          @mirrors << builder.mirror
          builder.mirror
        end
        
         * @param e
         * @param p
         * @return 
         */
        @Override
        public Object visitType(final TypeElement e, Object p) {
            System.out.println(e);
            Name className = e.getQualifiedName();
            int flags = 0;
            Type type = null;
            
            
            switch ( e.getKind() ){
                case INTERFACE:
                    flags |= Opcodes.ACC_INTERFACE;
                    type = Type.getType(className.toString());
                    break;
                case CLASS:
                    type = Type.getType(className.toString());
                    break;
            }
            
            
            return super.visitType(e, p); //To change body of generated methods, choose Tools | Templates.
        }
        
}
