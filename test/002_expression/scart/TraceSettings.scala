/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core-Test
 * package:org.stellabs.scart.config
 * file:002_expression/scart/TraceSettings.scala
 * 
 * Copyright 2013, 2014 Stellabs.
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


package org.stellabs.scart.config
import org.stellabs.scart.tracing.{SettingsBase, SettingsByDefault}

final object Settings extends SettingsByDefault
{
  import language.postfixOps
  import SettingsBase._

  protected val trace   = `ON`
  val priority          = 5
  val printer           = Printer     `System.err.println`
  val formatter         = Formatter   `all-first`
  
  // Example of canonical path to relativize against, for this test case in this very SBT environment
  override val relativizer = {
    new java.io.File(System.getProperty("user.dir") + "/../../../test/002_expression")
  }.getCanonicalPath
}

