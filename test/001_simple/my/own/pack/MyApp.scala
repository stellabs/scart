/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core-Test
 * package:my.own.pack
 * file:001_simple/my/own/pack/MyApp.scala
 * 
 * Copyright 2012-2014 Stellabs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package my.own.pack

import org.stellabs.scart.tracing._

object MyApp extends App{

  def computed_string = "computed string"
  
  private final val cond = false

  |++: $() // A trace that collects information, doesn't accept a string
  |++: $_ "Trace that may show only a literal string"
  |++: $$ s"Trace that may show a ${computed_string}"
  |++: %5 $$ "Trace that may show if the settings priority number is >= 5"
  |++: %cond $$ "Trace that won't show because the literal condition is false"
 
  println("Hello, world!")

  |--: $_ "A fast way to hard-code disable a trace" // more legible and searchable than commenting the trace
}