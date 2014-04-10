// /* collection:BlueStraggler         http://blue.straggler.org
//  * codebase:Scart
//  * toolset:Scart
//  * module:Project-Template-Build-Sbt
//  * package:sbt/template/
//  * file:inline/build.sbt
//  * 
//  * Copyright 2013, 2014 Stellabs.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *     http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

// NOTES: this build file, together with the folders/files under the same directory, form a PROJECT TEMPLATE BUNDLE
//
// * Relocation:
//   - the bundle can be copied elsewhere for basic projects (that don't use macros, have few dependencies & setups)
//   - the SETUP section below must be amended to reflect the tree structure of the newly adapted user project
//   - for projects with specific setups, dependencies and structures, the USER section must also be filled accordingly
//
// * Prerequisites:
//   - SBT 0.13.0 (might operate with newer versions as well)
//   - the Scart JAR must have been published (e.g. locally in the user's home dir) -the Scart build file does that-
//   - all the project's files, as well as this file, must be accessible from <project-home> or its subdirs. (b)(c)(d)
//
// * Usage:
//   - see the USER section, below.
//
// (a): <project-home> has only a referential purpose; it needs not be the actual user's source home, but a parent dir
// (b): <project-home> is computed by this build, based on the 'SLFromHomeToHere' value, set below
// (c): the path from <project-home> to the user source code's actual root is set in 'SLThisProjectSetup' 4th item
// (d): the target basedirname from <project-home> for user project transient data is set in 'SLThisBuildSetup' 1st item
// (e): paths relative to <project-home> MUST NOT contain ".." or ".". Behavior toward any symlink will depend on the OS


// |SETUP|

// Scart trace settings: switch(ON/OFF), priority-threshold, formatter-name, printer-name  
lazy val SLScartTraceSetup  = ("ON", 6, "all-first", "System.out.println")

// User project: organization, application-name, version-num, path-from-project-home-to-source-root(relative)
lazy val SLThisProjectSetup = ("ACME", "TheKillerApp", "0.00.000", "sbt/template/inline/killerapp/src")

// path-from-project-home-to-source-build-file(relative)
lazy val SLFromHomeToHere   = "sbt/template/inline/build.sbt" // This file

// Transient targets for build: from-project-home-to-user-target-basedirname(rel), generated-basedirname, target-suffix
lazy val SLThisBuildSetup   = ("sbt/template__inline", "generated", ".target")  // Can delete "**/*target" eventually

// The Scala to use for all this build phases; the same environment as the one that built the Scart JAR is recommended!
lazy val SLScalaVersion     = "2.10.4"

// Scart core library used by the build: organization, tool-name, tool-version, flavor
lazy val SLScartDependSetup = ("Stellabs", "Scart", "0.02.000", "Vanilla")


// |USER|

//************************** ADDITIONS OF SETTINGS & TASKS ARE BETTER DONE BELOW THIS LINE ****************************/
//
// For simple projects, it should be sufficient to just update the above items, then from the command-line:
//
// - `sbt "run <argument>*"` (e.g. `sbt "run with sbt"`); this command should be repeatable as long as:
//   - only the arguments vary (no recompilation)
//   - a file of the user project is modified (partial recompilation)
//   - the 'SLScartTraceSetup' items are changed in "build.sbt" (this file) -AND NOTHING ELSE- (total recompilation).
//
// Keep in mind that macros are involved. Avoid using the SBT "~" commands/tasks, or this build will become out of sync.
// If the build is out of sync, or if other parts of this file should be amended:
// - consider `sbt clean && sbt update` and/or use SBT build-maintenance commands/tasks etc.
// - OR, (current setup) remove transient data by deleting the "**/*target" dirs; next build will start from scratch
//                                          
// Hoping Scart and this build script will be useful to you!
//
//**************************** CHANGES IN THE TEMPLATE ARE BETTER DONE ABOVE THIS LINE ********************************/


// |PROJECT|

organization := SlOrganization

name := SlProjectName

version := SlProjectVersion

scalaVersion := SLScalaVersion

libraryDependencies += s"${SlStellabs}" %% s"${SlScartDeliverName}" % s"${SlScartVersion}"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value


// |SAFETY|

publishArtifact := false


// |BUILD| 

// Scart <project-home>
baseDirectory <<= baseDirectory / SlRelPathToHome

// Target is a sub-dir in the designated build setup root 
target <<= baseDirectory { _ / s"${SlMakeRootBasename}${SlTransientTgtSfx}" }

// Although not refered elsewhere in this build, the definition below specifies that it depends upon the inner project's
lazy val SlThisProject = project in file(".") dependsOn SlTrcGenProject

// The generated Scart trace settings class files must be included in the classpath for compile time...
managedClasspath in Compile += (classDirectory in Compile in SlTrcGenProject).value

// ... and for run time (haven't found a shortcut to designate all configurations at once)
managedClasspath in Runtime += (classDirectory in Compile in SlTrcGenProject).value


// |COMPILE| Only the test's Scart-settings against the Scart-Jar

// The files to compile are those of the user's source dir
scalaSource in Compile := baseDirectory.value / s"${SlProjectRoot}"

// Disable the Jar in general. Before enabling, the user should make sure whether to include a traces or not in her app
exportJars := false



// |INNER-BUILD|

// A handle on the dir that holds the user class files (using 'ThisProject' -not 'SlThisProject' to avoid recursion)
lazy val SlUserClassDir = classDirectory in Compile in ThisProject

// An inner project, whose automated build is purposed to generate the trace settings for Scart
lazy val SlTrcGenProject  = project in file(s"${SlGenDirName}") settings(
    //
    //  A number of build settings need to be defined because they are not disseminated from the main build.
    //  Below are those required for the build of the Scart settings object.
    //
    scalaVersion := SLScalaVersion,
    exportJars := false,
    //
    scalaSource in Compile <<= sourceManaged in Compile,
    //
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies += s"${SlStellabs}" %% s"${SlScartDeliverName}" % s"${SlScartVersion}",
    //
    // A Scala source file is automatically generated according to the above Scart Trace Setup.
    // In order to avoid generating the same contents multiple times, both last modification timings are compared with
    // each other, of the generated file and of this very file ('build.sbt'). If this file is younger, contents
    // are regenerated. If trace settings haven't changed, that's regeneration for nothing, but better safe than sorry!
    // Also, to make sure that SBT further proceed with TOTAL recompilation, ALL previous class files are deleted.
    sourceGenerators in Compile += task{
        val generatedDirName = (sourceManaged in Compile).value
        val generatedSrcFile = file(s"${generatedDirName}/GeneratedScartSettings.scala")
        val thisFile         = file("build.sbt")
        if (!generatedSrcFile.exists || generatedSrcFile.lastModified < thisFile.lastModified){
           println(s"""
*** (Re)generating Scart traces because of updates in "${thisFile.getName}" ***
"""               )
            //
            // By removing the directories that hold the class files, that forces the build to recompile
            IO.delete((classDirectory in Compile).value)
            IO.delete(SlUserClassDir.value)
            //
            // The Scart settings source file is just filled with values extracted from this build setup
            IO.write(generatedSrcFile, s"""/*
This file has been automatically (re)generated with SBT during the processing of:
${SLFromHomeToHere}
Any revisions done by hand will likely be lost!
*/
package org.stellabs.scart.config;
final object Settings extends org.stellabs.scart.tracing.SettingsByDefault
{
  import org.stellabs.scart.tracing.SettingsBase._;
  import language.postfixOps;
  protected val trace  = `${SlTrcOnOffSwitch}`;
  val priority         = ${SlTrcPrioThreshold};
  val formatter        = Formatter       `${SlTrcFormatter}`;
  val printer 	       = Printer         `${SlTrcPrinter}`;
}
"""                 )
        }
        // The result of this task is a sequence of source files
        IO.listFiles(generatedDirName, "*.scala").toSeq
    }
)


// |INTERNAL|

lazy val slReversePath      = { (fullname:String, isDir:Boolean) =>
    val numOfNames = (fullname split '/').filter{ n => ! (n.isEmpty || "." == n) }.size
    Array.fill(if (isDir) numOfNames else (numOfNames-1))("..") mkString "/"
}

lazy val SlRelPathToHome    = slReversePath(SLFromHomeToHere, false)

lazy val SlStellabs         = SLScartDependSetup._1

lazy val SlScartName        = SLScartDependSetup._2

lazy val SlScartVersion     = SLScartDependSetup._3

lazy val SlScartFlavor      = SLScartDependSetup._4

lazy val SlScartDeliverName = s"${SlScartName}${if(SlScartFlavor.isEmpty) "" else "-" + SlScartFlavor}" toLowerCase

lazy val SlOrganization     = SLThisProjectSetup._1

lazy val SlProjectName      = SLThisProjectSetup._2

lazy val SlProjectVersion   = SLThisProjectSetup._3

lazy val SlProjectRoot      = SLThisProjectSetup._4

lazy val SlMakeRootBasename = SLThisBuildSetup._1

lazy val SlGenerateBasename = SLThisBuildSetup._2

lazy val SlTransientTgtSfx  = SLThisBuildSetup._3

lazy val SlTrcOnOffSwitch   = SLScartTraceSetup._1

lazy val SlTrcPrioThreshold = SLScartTraceSetup._2

lazy val SlTrcFormatter     = SLScartTraceSetup._3

lazy val SlTrcPrinter       = SLScartTraceSetup._4

lazy val SlGenDirName = s"${SlGenerateBasename}${SlTransientTgtSfx}"


// |END|
