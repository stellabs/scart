// /* collection:BlueStraggler         http://blue.straggler.org
//  * codebase:Scart
//  * toolset:Scart
//  * module:Project-Test-Build-Sbt
//  * package:sbt/test/
//  * file:001_simple/build.sbt
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
// - prerequisite        : the Scart Jar must live under the Scart target dir (see Scart build)
// - build & run the test: `sbt run`
// - clean the test      : `sbt clean` (but SBT leaves stuff behind; eventually remove `**/*target` dirs)
// - before rebuilding   : `sbt update`


// |SETUP| This is a TEST build file

 // Relative path from <project-home> to this file
lazy val SLFromHomeToHere   = "sbt/test/001_simple"

// Respective source roots for the test, of Scart settings and User project
lazy val SLDoubletSrcRoot   = ("test/001_simple/scart", "test/001_simple/my")

// Place where the SCART project has been built; this test uses the jar file from there
lazy val SLScartTargetRoot  = "sbt/scart.target"

// Scart core library used by the build: organization, tool-name, tool-version, flavor
lazy val SLScartDependSetup = ("Stellabs", "Scart", "0.02.001", "Vanilla")

 // Target root, prefix, suffix
lazy val SLTestTargetSetup  = ("sbt", "test__", ".target")

lazy val SLScalaVersion     = "2.11.0"


// |PROJECT|

organization := SlStellabs

version := SlScartVersion

scalaVersion := SLScalaVersion

exportJars := false

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value


// |BUILD| Configurations are 'Compile' to set the Scart Settings, 'Test' to compile and run the tests

// Scart <project-home>
baseDirectory <<= baseDirectory / SlRelPathToHome

// Target directory
target <<= baseDirectory { _ / s"${SlTgtRoot}/${SlTgtPrefix}${SlTestName}${SlTgtSuffix}" }

// The pre-generated Scart-Jar is required to compile and run the tests
unmanagedBase <<= baseDirectory { _ / s"${SLScartTargetRoot}/${SlScalaBinVerName}" }

// This project depends on the inner builds
lazy val SlThisProject = project in file(".") dependsOn (SlSettingsProject)

// The generated Scart trace settings class files must be included in the classpath for compile time...
managedClasspath in Compile += (classDirectory in Compile in SlSettingsProject).value

// ... and for run time (haven't found a shortcut to designate all configurations at once)
managedClasspath in Runtime += (classDirectory in Compile in SlSettingsProject).value


// |COMPILE| Only the test's Scart-settings against the Scart-Jar

scalaSource in Compile := baseDirectory.value / s"${SlTestingSrcRoot}"

unmanagedJars in Compile <<= unmanagedBase map { dir => (dir ** SlScartJarName).classpath }


// |TEST| NONE! This IS the test build and is performed via `sbt run`


// |INNER-BUILD|

lazy val SlSettingsProject = project in file("settings.project") settings(
    baseDirectory <<= baseDirectory / s"${SlRelPathToHome}/..",
    target := file("settings.project/settings.target") ,
    unmanagedBase <<= baseDirectory { _ / s"${SLScartTargetRoot}/${SlScalaBinVerName}" },
    scalaVersion := SLScalaVersion,
    exportJars := false,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    unmanagedJars in Compile <<= unmanagedBase map { dir => (dir ** SlScartJarName).classpath },
    scalaSource in Compile := baseDirectory.value / s"${SlSettingSrcRoot}"
)


// |INTERNAL|

lazy val slReversePath      = { (fullname:String, isDir:Boolean) =>
    val numOfNames = (fullname split '/').filter{ n => ! (n.isEmpty || "." == n) }.size
    Array.fill(if (isDir) numOfNames else (numOfNames-1))("..") mkString "/"
}

lazy val SlTestName         = SLFromHomeToHere split '/' last

lazy val SlSettingSrcRoot   = SLDoubletSrcRoot._1
    
lazy val SlTestingSrcRoot   = SLDoubletSrcRoot._2

lazy val SlStellabs         = SLScartDependSetup._1

lazy val SlScartName        = SLScartDependSetup._2

lazy val SlScartVersion     = SLScartDependSetup._3

lazy val SlScartFlavor      = SLScartDependSetup._4

lazy val SlScartDeliverName = s"${SlScartName}${if(SlScartFlavor.isEmpty) "" else "-" + SlScartFlavor}" toLowerCase

lazy val SlRelPathToHome    = slReversePath(SLFromHomeToHere ,true)

lazy val SlTgtRoot          = SLTestTargetSetup._1

lazy val SlTgtPrefix        = SLTestTargetSetup._2

lazy val SlTgtSuffix        = SLTestTargetSetup._3

lazy val SLVersionRegex     = """(\d+)\.(\d+)\.(.+)""".r    // Major.Minor.Revision

lazy val SlScalaMajorMinor  = { val SLVersionRegex(major, minor, _) = SLScalaVersion; s"${major}.${minor}" }

lazy val SlScalaBinVerName  = s"scala-${SlScalaMajorMinor}"

lazy val SlScartJarName     = s"${SlScartDeliverName}_${SlScalaMajorMinor}-${SlScartVersion}.jar"


// |END|
    


