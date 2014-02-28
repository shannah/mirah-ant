package mypkg

class MirahClass < JavaClass
    def doSomething
        i = self.getInt
        puts i
    end
end

mc = MirahClass.new
mc.doSomething