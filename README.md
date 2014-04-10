Notice
------

All information and materials in this publication are provided by Stellabs and
contributors Òas isÓ and any express or implied warranties (including, but not
limited to, the implied warranties of merchantability and fitness for a
particular purpose) are disclaimed. In no event shall Stellabs or contributors
be liable for any direct, indirect, incidental, special, exemplary, or
consequential damages (including, but not limited to, procurement of substitute
goods or services; loss of use, data, or profits; or business interruption)
however caused and on any theory of liability, whether in contract, strict
liability, or tort (including negligence or otherwise) arising in any way out of
the use of this publication's information or materials, even if advised of the
possibility of such damage.

*******************************************************************************
#                  	   [BlueStraggler] Scart 
*******************************************************************************

**SCART** is a *lightweight tracing tool* for Scala that generates textual
debugging information.

Keeping the impact on readability _to a minimum_, Scart allows developers to
**insert traces** in the source code _without bloating_ the final binaries.

_The syntax is being defined, and the documentation is a work in progress._  
_But an early implementation was used to fix challenging bugs in a real-life_
_application. That was the motivation for kicking off Scart as a new project._

The source code is licensed under the Apache License, Version 2.0 

------------------------------------------------------------------------------

Getting started
---------------

* Prerequisites
 - Mercurial or Git (only in order to clone the repo)
 - JDK (if bytecode verification is required) or JRE
 - SBT 0.13.0
 - a command line shell (_e.g._ bash)
 - (Scala compiler for 2.10; SBT will download it automatically if absent)

* Cloning the repository
 - in the case of a _public_ repo, follow the hosting service's instructions
 
* Changes about the build
 - instructions to build/test/run Scart _without_ SBT are _no longer_ provided
  
------------------------------------------------------------------------------

Building Scart from the source
------------------------------

* Scart Core  
 - in `sbt/scart` do `sbt compile package publishLocal`  

* Testing  
 - in `sbt/test/*` do `sbt run`

> **NOTES**
> 
> - _see_ the respective `build.sbt` for more information
> - currently, the core JAR must exist _before_ tests can be run on it
   
------------------------------------------------------------------------------

Usage
-----

The documentation remains to be done. For the time being, it's possible to
proceed with either options that follow.

* Using the tests as models  
  Although they have exhaustive contents (therefore _not_ good examples of
  readability), the current tests basically _are_ applications that use Scart to
  emit traces. Thus it's possible to read them to understand how to combine
  the Scart JAR and its settings with a given application, in order to emit
  debug traces:
 - designate settings based on `test/**/scart/TraceSettings.scala`  
 - set traces as demonstrated in other source files `test/**/*.scala`  
 - create or modify a `build.sbt` file; it can be modeled on
   `sbt/test/**/build.sbt`
 - in the directory that contains the application's `build.sbt`, run `sbt`
   with the applicable arguments
  

* Using the extra template 
  A basic project template is available in `sbt/template/inline`.
 - copy the entire contents of the `inline` folder at your favorite place  
 - for a new project, use the source template `killerapp/src/KillerApp.scala`  
 - add traces in your source files if you want to
 - update `build.sbt` with the data of your project (_e.g._ paths and names)  
 - also designate your settings in `build.sbt`, the source file will be
   generated
 - from the command line, do: `sbt run` or `sbt "run <arguments>"`, that builds
   and executes the application  

  For more, please follow the instructions written in `build.sbt`.

> **IMPORTANT CONSIDERATIONS**  
> 
> Traces aren't phantom constructs. Depending on its kind, a trace _does_
> evaluate to **`Unit`** or to the **type of the expression** with which, as
> seamlessly as possible, it combines. When it is disabled or ignored, its type
> won't change (type-safety is preserved), only its behavior will change just to
> remove any related tracing information.
> 
> Notably, traces that result in `Unit` should **never** be inserted in places
> such as:  
> 
> - between an `if` and its non-enclosed result  
> - between a `for`/`while` and its non-enclosed body  
> - as the last statement of non-`Unit` expressions
> (e.g. in case of an implicit `return`)
> 
> => if inserting `{}` there would alter the behavior, that holds true with
> traces of that kind too.  
> 
>
> **Avoid creating side-effects** in traces, for example:  
>
> - when tracing with interpolated strings _e.g._
> `s"My counter is: ${myCountVar+=1; myCountVar}"`  
> - subtler, by using *lazy vals* or *by-name parameters* that haven't been
> used yet
> 
> => that's because the behavior will be altered when traces are enabled
> (and only when enabled, in both these cases).  

------------------------------------------------------------------------------

Disabling the traces
--------------------

Finally, there's **NO NEED** to remove Scart traces from the source code at the
end:  
just change, in the trace settings  
    ``val trace  = `ON` ``  
to  
    ``val trace  = `OFF` ``  
then recompile the application entirely and it will get rid of them all.

In order that the new settings take effect, the dependent project(s) should be
cleaned. _For example_, the test cases can be re-run with:
    `sbt clean run`

With the `OFF` switch, not only the traces won't show at run time, better, they
won't exist anymore at the Java bytecode level.  
No binary bloat!  

> **NOTES**
> 
> Should that be necessary, the presence (or absence) of traces in the bytecode
> can be verified with _e.g._ the  command:  
> `javap -c -private -classpath <dirs-and-jars> <class-qualified-basename>`
> By so doing, the content of dumped JVM instructions from classes is expected
> to be far more lightweight when the traces are disabled (as though there were
> no traces in the source code at all, with the exceptions already mentioned).  
> If that's not the case, there is probably a build issue; the project must be
> cleaned in order to remove the cached classes and rebuild it from scratch.  
> Conversely, since enabling traces implicitly adds calls, strings etc. the
> content can become quite heavyweight (traces inserted in the source code are
> similar to syntactic sugar, but they can abstract powerful features).  
> In comparison, most logging tools leave the same bytecode regardless of the
> actual settings. That is normal since they serve a different purpose:
> providing information even after deployment. Conversely, Scart is meant to
> help developers fixing bugs in the code; depending on the settings the amount
> of debug info can be huge and not proper to emit during operations.

------------------------------------------------------------------------------

[BlueStraggler]: http://blue.straggler.org

Copyright 2013, 2014 Stellabs. All rights reserved.
