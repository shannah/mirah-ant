#Mirahc Ant Task

An ant task for the [Mirah](http://www.mirah.org/) compiler.  This task also includes features not available in the standard Mirah compiler.  Specifically this task compiles both Mirah and Java files (by wrapping the javac task), and supports two-way dependencies between the mirah and java code.

##License

Apache 2.0

##Features

* Task can be used to compile .java and .mirah files in one swipe.
* Supports two-way dependencies between Java and Mirah code.

##Status

Alpha - This seems to work on all of the inputs I have provided so far, but I haven't built anything in production with it yet (as of July 2014).  In addition, the Mirah language is still evolving, and many of its features haven't been documented.


##Installation

1. Download [MirahAnt.jar](https://github.com/shannah/mirah-ant/raw/master/dist/MirahAnt.jar), [mirah.jar](https://github.com/shannah/mirah-ant/raw/master/lib/mirah.jar), and [mirahc.jar](https://github.com/shannah/mirah-ant/raw/master/lib/mirahc.jar).  You will reference them in your `classpath` attribute of the `taskdef` tag.

##Usage

1. Add the mirahc task to your build script.

~~~
<taskdef name="mirahc" classpath="paht/to/MirahAnt.jar:path/to/mirah.jar:path/to/mirahc.jar" classname="ca.weblite.mirah.ant.MirahcTask"/>
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

##Netbeans Plugin

This task is used as part of the Mirah Netbeans Plugin.  If you want IDE integration for Mirah, you should check out that plugin.

##Credits

1. This ANT task developed and maintained by [Steve Hannah](http://sjhannah.com).
2. Mirah (formerly Duby) created by [Charles Nutter](https://github.com/headius), and maintained by a small, but enthusiastic, team led by [ribrdb](https://github.com/ribrdb) and [baroquebobcat](https://github.com/baroquebobcat).

