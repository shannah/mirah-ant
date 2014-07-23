#Mirahc Ant Task

An ant task for the Mirah compiler.

##License

Apache 2.0

##Features

* Task can be used to compile .java and .mirah files in one swipe.
* Supports two-way dependencies between Java and Mirah code.

##Installation

1. Download [MirahAnt.jar](), [mirah.jar](), and [mirahc.jar]().  You will reference them in your `classpath` attribute of the `taskdef` tag.

##Usage

1. Add the mirahc task to your build script.

~~~
<taskdef name="mirahc" classpath="MirahAnt.jar:mirah.jar:mirahc.jar" classname="ca.weblite.mirah.ant.MirahcTask"/>
~~~

2. Use the mirahc task in your build script.  E.g.

~~~
<mirahc dest="build/classes">
  <javac bootclasspath="…" 
         destdir="…"  
         includeantruntime="false" 
         source="1.5" 
         sourcepath="${javac.source}:src" 
         srcdir="src" 
         target="1.5">
    <classpath>
      <path path="..."/>
    </classpath>
    <compilerarg line="${javac.compilerargs}"/>
  </javac>
</mirahc>
~~~

I.e. It wraps a javac task call, draws off of the settings provided to `javac`.



