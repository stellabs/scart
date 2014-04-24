/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core-Stub
 * package:org.stellabs.scart.config
 * file:stub/ScartSettings.scala
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
 
package org.stellabs.scart.config

import language.postfixOps
import org.stellabs.scart.tracing.{SettingsBase, SettingsByDefault}
import org.stellabs.scart.tracing.SettingsBase._

/* WARNING: This is a Stub whose classfile MUST NOT be provided with SCART.
 *  
 *  The Settings are MISSING ON PURPOSE in Scart:
 *  Scart won't operate without them, so the developer must provide her own Scart Settings in her project.
 *  Also she has to keep in mind that her project MUST BE FORCIBLY RECOMPILED whenever she changes the Settings;
 *  in order to let the Scart MACROS regenerate the proper traces.
 *  (if not the entire project, at least the Scala files whose source code makes use of Scart)
 *  
 */


final object Settings extends SettingsBase with SettingsByDefault
{
  protected val trace		= `OFF`
  val priority          = LEAST_PRIORITY
  val formatter         = Formatter `smoker`
  val printer 	        = Printer `System.err.println`
}
