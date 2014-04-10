/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core-Test
 * package:org.stellabs.scart.config
 * file:001_simple/scart/TraceSettings.scala
 * 
 * Copyright 2012, 2013 Stellabs.
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


/** SCART Settings
 * 
 *  // 'trace' designates whether to enable or blanket-disable the trace feature
 *  val trace = `ON`   // OR,
 *  val trace = `OFF`  // the bytecode produced by traces is the same as a void Unit:(), hence no bytecodes.
 * 
 *  // 'priority' designates the threshold for which traces are really processed. Priority 1 in a trace is the highest.
 *  // (0 (zero) in the settings means PROCESS ALL, however; and 0 must not be used directly in traces) 
 *  val priority = 2   // if a trace has a priority of 3 or more, it will be disabled and minimal bytecode is generated
 *  
 *  // 'formatter' is the Data => String function that formats the trace data; developers may add features (e.g.: date)
 *  val formatter = Formatter `as-is`  // produces nothing else each trace's original than the original String entry
 *
 *  // 'printer' is the String => Unit function that prints traces; developers may add features (e.g.: flush in a file)
 *  val printer  = Printer `System.err.println` // a basic printer using the synonymic method
 * 
 * WARNING:
 *  The dependent project MUST ALSO be cleaned after any change below, otherwise it might use out-of-date settings!
 *  
 */


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

