package ca.weblite.asm.testcode
import ca.weblite.asm.testcode.TestJavaClass.InternalClass
class TestMirahClass
    def hello
       puts "here"
       TestJavaClass::hello2
       InternalClass::hello3
       iclass = InternalClass.new
       foo = ""
       foo = iclass.getHello
    end
end