/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core-Test
 * package:user
 * file:002_expression/main/Main.scala
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

package user

import org.stellabs.scart.tracing._

object Main extends App{
  
  println( "=========== Start ============" )

  def methodByName(fn: String=>Float, msg: => String){   
    fn(s"The message is ${msg e_++: "msg"}.")  e_++:  s"""methodByName($fn,"$msg")""" 
    // The above raises a warning if the switch is `OFF`, however the following wouldn't:
    // s"""methodByName($fn,"$msg")""".e_++:{ fn(s"The message is ${msg e_++: "msg"}.") }
  }
  
  lazy val lzval  = { /* unit */ }      e_++: "unit"
  lazy val lzval4 = 'a_symbol           x_++: ({case $B(_)=>"sym";case $E(r)=>s" eval: $r";case $X(x)=>s"$x"}:$e) 
  lazy val lzval5 = "a string"          x_++: "lzval5"
  lazy val lzval6 = 2.0                 e_--: "disabled expression trace"
  lazy val lzval7 = 3.0                 x_++: 'lzval7
  
  lazy val lzExcept1 = {                ;|++: $_ "an exception is coming"
    ???                                 ;|++: $_ "this trace won't show because evaluation won't complete"
  }                                     e_++: "lzExcept1"
  
  lazy val lzExcept2 = "lzExcept2".e_++:{ ??? }
  lazy val lzExcept3 = "lzExcept3".x_++:{ ??? }
  
  println( "--------- some values --------" )
  val val1 = 11 * 4 - 2                 e_++: ({case $B(_)=>"Expecting 42: ";case $E(r)=>""+r;case $X(x)=>"?"}:$e)
  println( s"The answer to life, the macro universe and everything is: ${val1}" ) 
  val val2 = {}                         e_++: "val2"
  val val3_4 = "trace w/priority 5"     e_++: 4->({case $B(_)=>"";case $E(r)=>s"""val3_4: "$r"""";case $X(x)=>"?"}:$e)
  val val3_5 = "trace w/priority 5"     e_++: 5->({case $B(_)=>"";case $E(r)=>s"""val3_5: "$r"""";case $X(x)=>"?"}:$e)
  val val3_6 = "no trace w/priority 5"  e_++: 6->({case $B(_)=>"";case $E(r)=>s"""val3_6: "$r"""";case $X(x)=>"?"}:$e)
  val val3_0 = "no trace: disabled"     e_--: 1->({case $B(_)=>"";case $E(r)=>s"""val3_0: "$r"""";case $X(x)=>"?"}:$e)
  val val4_4 = "trace w/priority 5"     e_++: 4-> "val4_4"
  val val4_5 = "trace w/priority 5"     e_++: 5-> "val4_5"
  val val4_6 = "no trace w/priority 5"  e_++: 6-> "val4_6"
  val val4_0 = "no trace: disabled"     e_--: 1-> "val4_0"

                                        ;|++: $_ "lazy vals have not been evaluated yet"

  println( "--------- lzExcept1 ----------" )
  try println( s"lzExcept1 is ${lzExcept1}" ) catch { case _:Throwable => println("Exception caught") }
  println( "--------- lzExcept2 ----------" )
  try println( s"lzExcept2 is ${lzExcept2}" ) catch { case _:Throwable => println("Exception caught") }
  println( "--------- lzExcept3 ----------" )
  try println( s"lzExcept3 is ${lzExcept3}" ) catch { case _:Throwable => println("Exception caught") }

  println( "------ other lazy vals -------" )
  println( s"lzval6 is ${lzval6}" )
  println( s"lzval7 is ${lzval7}" )
  println( s"""lzval5 is "${lzval5}"""" )
  println( s"lzval4 is ${lzval4}" )
  println( s"lzval is ${lzval}" )

  println( "------- method calls ---------" )  
  val prt = { s:String => println(s); s.length.toFloat }
  val message = "Hello, World!"
  methodByName(prt, message)  
  methodByName(prt, "Goodbye")
  
  println( "------- outer & inner --------" )  
  val val_oi1 = 2
  def g(i:Int) = i+1                    e_++: s"g($i)"
  def f(i:Int) = g(i)*2                 e_++: s"f($i)"
                                        
  val val_oi2 =                         "val_oi2-outer".e_++:
  {                                     "val_oi2-inner".e_++:
    {                                   
      f(val_oi1)                        e_++:  "val_oi2-innermost"
    } + 1                               e_++:  s"adding 1 to f(arg)\nwhere arg:${val_oi1.$rt} = ${val_oi1}\n"
  }

  println( "------- types --------" )  
  |++: $$ s"${this}'s type is:\n${this.$rt} (raw)\n${this.$wt} (widen)\n${this.$nt} (normalized)"

  println( "===========  End  ============" )
} 