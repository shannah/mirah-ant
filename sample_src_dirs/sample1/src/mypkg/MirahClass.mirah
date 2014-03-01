package mypkg

class MirahClass < JavaClass
    def doSomething
        i = self.getInt
        puts i
    end
end

mc = MirahClass.new
mc.doSomething
strArr = ['1','2','3'].toArray(String[0])
puts strArr