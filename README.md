Disclaimer
----------

All information and materials in this publication are provided by Stellabs and
contributors "as is" and any express or implied warranties (including, but not
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

**[SCART]** is a **_lightweight tracing tool for Scala_** (2.10 & 2.11) that
generates textual debugging information.

Keeping the impact on readability _to a minimum_, Scart allows developers to
**insert [traces]** in the source code **without bloating** the deliverables.

_The syntax is being defined, and the documentation is a work in progress._
_But an early implementation had been made to identify challenging bugs in a_
_real-life application. It successfully helped pinpoint the issues, hence fix_
_the code, and gave us the motivation to kick off a cleaner implementation_
_as an open-source project_.

The source code is released under the **Apache License**, Version 2.0 

------------------------------------------------------------------------------

Getting started
---------------

### Prerequisites
Scart now operates with either Scala 2.10.x or 2.11.0  
_(required version to be specified in `build.sbt`)_

* SBT 0.13.0 (it will download the Scala compiler automatically if necessary)
* Mercurial or Git (only in order to clone the repository)
* JDK (if bytecode verification is required) or JRE
* a command line shell (_e.g._ bash)


### Notable changes
* v0.02.000
 - Instructions to build/test/run Scart _without_ SBT are _no longer_ provided
 - Replica on GitHub: https://github.com/stellabs/scart
* v0.02.001
  - Improvement of this outline documentation
  - Support of Scala 2.11.0


### Cloning the repository
In the case of a _public_ repository, follow the hosting service's instructions.

 
### Building Scart from the source
This describes how to build and test from the command-line using SBT.

#### Scart Core
To compile, create and publish a Jar on your system, do as follows in
the `sbt/scart` directory:

* edit `build.sbt` to update the Scala version you want to compile against 
* execute `sbt publishLocal`  
 
#### Testing the build
The current tests require human verification, as opposed to automated tests.
In the `sbt/test/*` directories:

* edit `build.sbt` to update the Scala version, using the same as the core 
* execute `sbt run`

> **NOTES**
> 
> - _see_ the respective `build.sbt` for more information
> - due to macro stubs, the core JAR must exist _before_ tests can be run on it

------------------------------------------------------------------------------

Usage
-----

### Tracers

#### Line Tracers
An LT's is a basic tracer, the syntax is either as follows:

* `|++: $$` _string_
* `|++: %`_PRIORITY_ `$$` _string_

where:

* _string_ can be either:
 - a literal string that could identify _e.g._ an event or position
 - an interpolated string that refers to items that have been _already_
   _evaluated_, its usage besides that is free
* _PRIORITY_ is a literal `Int` of value 1 or more
  if a tracer has a priority greater than the Settings', it becomes disabled
* _Literal_ means something which is recognized as such by the compiler
  _i.e._ most identifiers aren't literals, but a macro could yield one

`|++:` is *not* an operator and should be used as a statement. It displays the
string in a trace with useful information, when it gets evaluated, and returns
the `Unit` value `()` (unless an exception occurs in the interpolated string).

In an attempt to improve the legibility of the original source code, a possible
style is to align the LT with the ET, on the same column on the right side.

#### Expression Tracers
They represent the most useful and unique feature of Scart. An ET can evaluate
seamlessly to the **result of the expressions** with which it combines. Also,
since an ET is a least-priority right-associative operator, most times it may
just be appended to the right-end of an existing line of source code. Moreover,
by adding extra spaces before the tracer, it's possible to _visually_ separate
the "real" code from the parts which are dedicated to traces and have no added
value algorithmically speaking.

Expression Tracers are therefore very convenient to use. The syntax is either:

* _expression_ `e_++:` _'identifier_
* _expression_ `e_++:` _string_
* _expression_ `e_++:` _PRIORITY_ `->` _string_

where, in addition to the explanations of the previous section:

* _expression_ is any Scala expression
  (even `void`/`Unit` method calls evaluate to `()` thus are expressions)
* _'identifier_ is a Scala literal `Symbol` used to identify the trace or
   the expression (_e.g._ vals, vars, objects)

`e_++:` shows in the traces, by default, when the expression gets evaluated
(and not before, _e.g._ `lazy val`):

 * the identifier or string
 * the result of the expression, as a `String` (using `Object#toString`)
 * also the exception, if it catches one (that it re-throws)
 
However, **catching and re-throwing exceptions isn't always best** because,
as a side effect, the exception stack will memoize a position in the tracer.
That can be avoided by replacing `e_++:` with `x_++:`.

The latter:

* doesn't catch exceptions but can detect one; that shows in the trace
* however, it is unable to identify the exception: the trace gives no ID

#### Fast inhibition
Replacing the `++` part with `--` within an LT or an ET disables it at the
source code level. Although that ought to be done as a temporary solution
only, for instance to remove a noisy trace, that is convenient sometimes:

* the whole project containing that source file need not be cleaned just to
  disable the trace
* it's more legible and searchable (_e.g._ with grep) than commenting the trace
  when the developer wants to inhibit the tracer rather than deleting it


### Trace settings

The Scart JAR **doesn't include default settings**, on purpose, to avoid
mistakes such as the delivery to a customer of an application that contains
traces. Instead, the control is left to the developers and/or people responsible
of the build. Therefore, the Settings have to be provided or Scart won't work
and build errors will appear.

_For instance_, a source code file that designates the settings could have the
following content:

```scala
package org.stellabs.scart.config
import org.stellabs.scart.tracing.{SettingsBase, SettingsByDefault}

final object Settings extends SettingsByDefault
{
  import language.postfixOps
  import SettingsBase._

  protected val trace  = `ON`
  val priority         = 5
  val formatter        = Formatter       `all-last`
  val printer          = Printer         `System.err.println`
}
```

where:

* `trace` is the main switch for enabling (`ON`) or disabling (`OFF`) tracing
* `priority` is a `Int` of value 0 (highest) or more. If a tracer has a
  priority, its trace will be output only if its value is equal or less than
  the value of this settings priority. Tracers without priorities always emit
  unless, of course, the switch is `OFF`.
* `formatter` designates under which format each trace will be emitted:
 - `as-is`: only the entry string/identifier
 - `all-first`: the entry is followed by its source file name, line#, column#,
    class or method name
 - `all-last`: the entry is preceded with its source file name, line#, column#,
    class or method name
 - `inclosure-only`: the entry is _not_ emitted, only the class or method name
   is (useful for example to identify endless loops without all the clutter)
* `printer`: is the `String => Unit` function that actually outputs the traces.
  Developers may provide their own function, but there are predefined printers:
  - `System.out.println` uses its synonymic method
  - `System.err.println` uses its synonymic method

> **NOTES**
>
> * the highest priority applicable in tracers must be 1 (one), as opposed to
>   the Settings object's 0 (zero)
>
> * For demonstration purposes, in this document, traces are printed via
>   `System.err.println`. However, the Setting's `printer` could use a Logger,
>   for instance by the definition of something _like_:
>
>   ```scala
>   val printer = {s:String => getLogger(...).trace(..., s)}
>   ```
>
>   provided that the `getLogger` method (or function) and its arguments are
>   defined _before_ the project's compilation (as it is required in the case
>   of macros).

The `Settings` object must be compiled ahead-of-time:

* the Scart JAR contains macros; the compiler invokes them when they are used
in a project's source code
* the settings designate how the Scart macros behave if invoked; in the example
above, traces of priority 6 or more won't be generated at all (nothing at the
bytecode level) even if they exist in the source code

------------------------------------------------------------------------------

Output
------

Given the following source code, as a tiny example:

```scala
package com.example

import org.stellabs.scart.tracing._

object KillerApp extends App{           ;|++: $$  "Goodbye, Underworld!"
  val str = args.mkString(" ")          e_++: 'str  
  println(s"Hello, World! ($str)")      e_++: 5-> "println(...)"
}
```

With the Settings set as illustrated earlier, `sbt "run a b c"` will output
something like:

```
> /sandbox/killerapp/src/KillerApp.scala(L5,C44)[class com.example.KillerApp]: Goodbye, Underworld!
> /sandbox/killerapp/src/KillerApp.scala(L6,C44)[class com.example.KillerApp]: str="a b c"
Hello, World! (a b c)
> /sandbox/killerapp/src/KillerApp.scala(L7,C45)[class com.example.KillerApp]: println(...)=()
```
    
where the traces, preceded with `> `, show respectively:

* the source file name, line#, column#, class name and the LT's literal string
* the contents of `str`, the next `println` shows the ET didn't alter it
* the `println`'s return value: `()` as it's `Unit`

The last Expression Tracer's priority being 5 (`5-> ...`). If the Setting's
priority is changed to 4 (`val priority = 4`) and the project cleaned, that
trace won't show as illustrated by the new output:

```
> /sandbox/killerapp/src/KillerApp.scala(L5,C44)[class com.example.KillerApp]: Goodbye, Underworld!
> /sandbox/killerapp/src/KillerApp.scala(L6,C44)[class com.example.KillerApp]: str="a b c"
Hello, World! (a b c)
```

Finally, all tracers can be inhibited by switching them off in the Settings,
``protected val trace  = `OFF` ``; that gives the output:

```
Hello, World! (a b c)
```

------------------------------------------------------------------------------

Features & How-tos
------------------

There are many more features than illustrated above, but a proper documentation
remains to be done. For the time being, it's possible to proceed with the options
that follow.

* Using the tests as models _(recommended)_
  Although they have exhaustive contents (therefore _not_ good examples of
  readability), the current tests basically _are_ applications that use Scart
  to emit traces. Thus it's possible to read them to understand how to combine
  the Scart JAR and its settings with a given application, in order to emit
  debug traces:
 - designate settings based on `test/**/scart/TraceSettings.scala`  
 - set traces as demonstrated in other source files `test/**/*.scala`  
 - create or modify a `build.sbt` file; it can be modeled on
   `sbt/test/**/build.sbt`
 - in the directory that contains the application's `build.sbt`, run `sbt`
   with the applicable arguments
  
* Using the extra template _(quick and easy)_
  A basic project template is available in `sbt/template/inline`. Developers
  without prior exposure to SBT from the command-line might prefer to start
  with this solution.
 - copy the entire contents of the `inline` folder at your favorite place  
 - for a new project, use the source template `killerapp/src/KillerApp.scala`  
 - add traces in your source files
 - update `build.sbt` with the data of your project (_e.g._ paths and names)  
 - also designate your settings in `build.sbt`, the source file will be
   generated
 - from the command line, do: `sbt run` or `sbt "run <arguments>"`, that
   builds and executes the application  

  For more, follow the instructions written in the respective `build.sbt`.

> **IMPORTANT CONSIDERATIONS**  
> 
> Tracers, the mechanisms that produce traces, aren't phantom constructs.   
> The 2 kinds of tracers  _do_ evaluate to something.
>
> * **Line Tracers** should be used like statements and are **`Unit`**.
> * **Expressions Tracers** _don't change the value or the type_ of an
>   expression, as explained earlier.
>
> When a tracer is disabled or inhibited, its type won't change (type-safety
> is preserved), only its behavior will change, to not output any tracing
> information, _aka_ trace, that it would emit otherwise.
>
> A tracer's insertion in the source code is thus quite seamless. However,
> Line Tracers should **never** be inserted in places such as:  
> 
> - between an `if` and its non-enclosed result  
> - between a `for`/`while` and its non-enclosed body  
> - as the last statement of non-`Unit` expressions
> (e.g. in case of an implicit `return`)
> 
> => if inserting `{}` somewhere would alter the behavior, inserting a
> Line Tracer there would alter the behavior as well.  
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


### Using Scart within an IDE

As with [Scala macros], **each project** that makes use of Scart should depend
on a separate _"Settings project"_ that defines the Scart Settings object:

* the user project must have a dependency on the Settings project
 - optionally depend on the Scart JAR, that varies with the configuration below
* the Settings project must either:
 - depend on the Scart JAR
 - contain the Scart Core source(s) (but exclude the Scart stub(s), tests etc.)
 
> **NOTES**
>
> * if Scart was built using `sbt publishLocal`, its JAR should exist somewhere
>   under the developer's home directory.
> * the Scart core source code is under `compile/main/src/` in this repository.
> * when the Settings project change, it should be rebuilt, then the user
>   project should be cleaned and rebuilt. Whether that happens automatically
>   or not might vary with the IDE.
> * Scart Core, Scart Settings and User Project should be all compiled using
>   the same version of the Scala compiler

------------------------------------------------------------------------------

Disabling the traces
--------------------

Finally, there's **NO NEED** to remove Scart traces from the source code at the
end in order to deliver or deploy:

* just change, in the trace settings  
    ``val trace  = `ON` ``  
  to  
    ``val trace  = `OFF` ``  
* then recompile the application entirely
  => your project will get rid of all traces.

In order to make sure that the new settings take effect, the dependent
project(s) should be cleaned. _For example_, the test cases released together
with the Scart core can be re-run with:
    `sbt clean update run`

With the `OFF` switch, not only the traces won't show at run time, better, they
won't exist anymore at the Java bytecode level.

No binary bloat!  

> **NOTES**
> 
> * Conversely, since enabling traces (switch `ON`) implicitly adds calls,
>   strings etc. the content can become quite heavyweight. Tracers inserted in
>   the source code are similar to syntactic sugar, but they can abstract
>   powerful features.  
> 
> * Should that be necessary, the presence (or absence) of traces in the
>   bytecode can be verified with _e.g._ the  command:  
>   `javap -c -private -classpath <dirs-and-jars> <class-qualified-basename>`  
>   By so doing, the content of dumped JVM instructions from classes is
>   expected to be far more lightweight when the traces are disabled (as though
>   there were no traces in the source code at all, with the exceptions already
>   mentioned).  
>   If that's not the case, there is probably a build issue; the project must
>   be cleaned in order to remove the cached classes and rebuild it from
>   scratch.  

In comparison, most logging tools leave the same bytecode regardless of the
actual settings. That is normal since they serve a different purpose:
providing information even after deployment. Scart, however, is meant to help
developers fixing bugs in the code; depending on the settings, the amount of
low-level debug information can be huge thus not proper to emit during
operations.

------------------------------------------------------------------------------

[BlueStraggler]: http://blue.straggler.org
[Scart]: https://github.com/stellabs/scart
[traces]: http://en.wikipedia.org/wiki/Tracing_%28software%29
[Scala macros]:
http://docs.scala-lang.org/overviews/macros/overview.html#using_macros_with_scala_ide_or_intellij_idea

Copyright 2013, 2014 Stellabs.
