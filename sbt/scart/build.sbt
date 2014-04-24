// /* collection:BlueStraggler         http://blue.straggler.org
//  * codebase:Scart
//  * toolset:Scart
//  * module:Project-Build-Sbt
//  * package:sbt/scart/
//  * file:build.sbt
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

// NOTES:
// - build the project: `sbt compile package` (produces Scart's Jar)
// - testing          : not done in this build but in separate test builds (interactive: requires human verification)
// - publish the Jar  : (after building) `sbt publishLocal`
// - clean the project: `sbt clean`(but SBT leaves stuff behind; eventually remove `**/*target` dirs)
// - before rebuilding: `sbt update`


// |SETUP|

 // Relative path from <project-home> to this file
lazy val SLFromHomeToHere   = "sbt/scart/build.sbt"

lazy val SLThisProjectSetup = ("Stellabs", "Scart", "0.02.001", "Vanilla")

lazy val SLScalaVersion     = "2.11.0"

// Inter-dependent sources can't be scattered in unrelated dirs when SBT is used; so they've moved under the same root
lazy val SLCommonSrcRoot    = "compile"                 // From <project-home>

// From <project-home>, the path to the dir that SBT produces as target
lazy val SLScartTargetRoot  = "sbt/scart.target"        

lazy val SLStubPackageName  = "org.stellabs.scart.config"


// |PROJECT|

organization := SlStellabs

name := SlDeliverableName

version := SlProjectVersion

scalaVersion := SLScalaVersion

scalacOptions += "-feature"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value


// |SAFETY| WOW! Does SBT publishing include all the source code by default!? (ain't insecure?) This is OSS, phew! :o)

publishArtifact := false 

publishArtifact in (Compile, packageBin):= true

publishArtifact in (Test, packageBin):= false


// |BUILD|

// sbt-related files are in <project-home>/sbt; sources are dispatched in several sub-dirs of <project-home>
baseDirectory <<= baseDirectory / SlRelPathToHome

// Target directory
target <<= baseDirectory { _ / SLScartTargetRoot }


// |COMPILE| Stubs are involved, that are inter-dependent with the main sources, meaning ALL MUST be compiled together

// Scart was not meant to be SBT-centric, but due to SBT's design choices, files have been moved for convenience
// Limitations with the current SBT implementation:
// - scalaSource accepts (/is) only 1 root
// - the same applies to sourceDirectory 
// - sourceDirectories accepts several roots, but the source in each root appears to compile separately from other roots
// => that's an N/A for sources that are inter-dependent AND in separate roots; e.g. as Scart was originally structured.
// In order to use SBT without trouble, stubs and core sources share now a same root directory.
// There are alternatives (e.g. automated file copying), but they come with an overhead and are less convenient.
scalaSource in Compile <<= baseDirectory { _ / SLCommonSrcRoot }


// |TEST| NO TESTS ARE DONE HERE for the time being!

// Exhaustive testing would require a more complex SBT build.
// Not only Scart uses Scala Macros, but the its settings are implemented as a stub for the sole purpose of compilation.
// - The way that the SBT 'test' task is implemented, the tests' compilation snatches the stub classes and it seems
//   impossible to exclude them in this setting file unless auxiliary build files are also involved.
// - However each test case should be preceded by a separate pre-compilation: while defining the Scart settings doesn't
//   create new macros, they do steer the Scart macros behavior -that have been compiled and stored in the Jar file.
// Therefore testing is more manageable with independent build files, one per (Test-Suite, Scart-Settings-object) pair. 
sourceGenerators in Test += task {
    System.err.println("""
    *** BE WARNED that NO TESTS ARE DONE HERE!!!             ***
    *** In order to proceed with testing, instead:           ***
    *** - first, produce a Scart Jar with `sbt publishLocal` ***
    *** - then, use the builds: "sbt/test/**/build.sbt"      ***
    """)
    Nil:Seq[File]
}
   

// |PACKAGE| Note that the JAR must be BUILT BEFORE the actual testing

// Include all Scart classes except the stub's; because actual components must be provided by user-projects instead.
// Currently there are stubs for Scart settings, discriminated by the 'config' sub-package
mappings in (Compile,packageBin) ~= {
    (_:Seq[(File, String)]) filter { case (_, toPath) => !(toPath contains s"${SlStubPackagePath}") }
}

// Disable the Jar in general...
exportJars := false

// However, enable the 'package' task for the source's classes
exportJars in Compile := true


// |INTERNAL|

lazy val slReversePath      = { (fullname:String, isDir:Boolean) =>
    val numOfNames = (fullname split '/').filter{ n => ! (n.isEmpty || "." == n) }.size
    Array.fill(if (isDir) numOfNames else (numOfNames-1))("..") mkString "/"
}

lazy val SlStubPackagePath  = { SLStubPackageName split '.' mkString "/" }

lazy val SlStellabs         = SLThisProjectSetup._1

lazy val SlProjectName      = SLThisProjectSetup._2

lazy val SlProjectVersion   = SLThisProjectSetup._3

lazy val SlFlavor           = SLThisProjectSetup._4

lazy val SlDeliverableName  = s"${SlProjectName}${if(SlFlavor.isEmpty) "" else "-" + SlFlavor}" toLowerCase

lazy val SlRelPathToHome    = slReversePath(SLFromHomeToHere, false)


// |END|

