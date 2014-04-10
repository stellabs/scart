/* collection:BlueStraggler         http://blue.straggler.org
 * codebase:Scart
 * toolset:Scart
 * module:Core
 * package:org.stellabs.scart
 * file:org/stellabs/scart/package.scala
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


/*
 *  CAUTION! THIS IS EXPERIMENTAL, AT LEAST AS MUCH AS SCALA MACROS
 *  
 *  KNOWN LIMITATIONS:
 *  - if an inner function is defined in an outer function, an inclosure from within the inner will refer to the outer
 */

package org.stellabs.scart

package tracing{
  import tracing._

  /*
   * SETTINGS
   */

  // Base class for a Settings singleton object
  final object SettingsBase{
  
    // Values for main switch
    val `ON`  = true;
    val `OFF` = false;

    // Priority levels, thresholds
    val HIGHEST_PRIORITY = 1;
    val LEAST_PRIORITY   = Int.MaxValue;
    
    // Type for the format methods
    type FormatterType = TraceData => String

    // Types for the printing methods
    type PrinterType      = String => Unit
    type FormatLinerType  = (String,String) => Unit   // 1st is pre-formatted string, 2nd is entry

  }
  trait SettingsBase{
    import SettingsBase._
    
    // ALL THE MEMBERS *MUST BE DEFINED* IN THE SETTINGS IMPLEMENTATION
    
	  protected[this] def trace:Boolean   // Enable/disable the traces
    protected[this] def priority:Int    // If traces enabled, designate the priority threshold of handled traces

	  // Format method of choice
	  def formatter: FormatterType;
	  
	  // Low-level printing method of choice
	  def printer: PrinterType

	  // Printing facilities
	  def liner   :PrinterType                 // for a single line of trace
	  def pftLiner:FormatLinerType             // ditto but for a pre-formatted entry
	  def enter   :FormatLinerType             // when entering the evaluation of an expression 
    def exit    :FormatLinerType             // when exiting, ditto (it's guaranteed it'll be invoked after an 'enter')
    def bexViewer(entry:String):$BEXViewer   // viewers for an expression
 	  val escape:String                        // escape-sequence string that's replaced with the entry before printing


	  // The ultimate decision that allows or forbids the trace
	  def doIt:Boolean

	  // An canonical-absolute path (null means to not use) in order to relativize Scala sources file paths in traces
	  def relativizer:String  // by using it messages may become shorter, and not depend from the user's environment 	
	  
	} // trait SettingsBase
	                                  
	
  trait SettingsByDefault extends SettingsBase{
	  import SettingsBase._
    
    // By default, the decision to trace or not just depends on the switch 
    def doIt = trace    // A finer-grained control may be reached by overriding it
    
    // By default, no path is used to relativize those in traces
    def relativizer = null:String
    
    // A collection of low-level printing methods
    object Printer {
      val `System.out.println`:PrinterType = s => {System.out.println(s); System.out.flush}
      val `System.err.println`:PrinterType = s => {System.err.println(s); System.err.flush}
    }
    
    // A collection of formatting methods
    object Formatter {
      
      // FOR TESTS
      val `nihilistic`:FormatterType = d => null                     // To eliminate traces, switch to `OFF` instead!!
      val `smoker`:FormatterType     = d => "This is a smoke test."  // Won't fail if the entry is problematical
      
      // No more, no less than the entry (except '\n')
      val `as-is`:FormatterType = { case TraceData(entry,_,_) => entry } 
      
      // Only the inclosure identification
      val `inclosure-only`:FormatterType =
      { case TraceData(_, Inclosure(kind, iname), _) => s"${kind.toString.toLowerCase} ${iname}" }
        
      // All with entry at 1st position
      val `all-first`:FormatterType ={
        case TraceData(entry, Inclosure(kind, iname), Location(path, line, col)) =>
          s"${entry}\t@${path}(L${line},C${col})[${kind.toString.toLowerCase} ${iname}]"
      }
    
      // All with entry at last position
      val `all-last`:FormatterType ={
        case TraceData(entry, Inclosure(kind, iname), Location(path, line, col)) =>
          s"${path}(L${line},C${col})[${kind.toString.toLowerCase} ${iname}]: ${entry}"
      }

    } // object Formatter
	 
	 
    // TRACE-LEVEL PRINTING FACILITIES
    
    // The zero-character is user as the default escape sequence
    val escape      = 0.toChar.toString; 
    
    // By-default viewers for expressions: expr'='result.toString or expr'<'exception.toString'>'
    def bexViewer(entry:String) = {
      case $B(t) if null==t  => entry
      case $B(t)             => s"$entry:$t"
      case $E(e:String)      => s"""="$e""""
      case $E(e)             => s"=$e"
      case $X(x) if null==x  => "<>"
      case $X(x)             => s"<$x>"
    }
    
    // The by-default handling of line, expression printing: indent the traces to ease their reading
    val traceStack  = collection.mutable.ArrayStack[String]()
    def liner    = s         => {                                         // for a single line of trace
      val indent      = "> "
      val linemark    = ">>> "
      val n = traceStack.size
      printer(indent*(1+n) + s.replace("\n", "\n" + indent*n + linemark)) 
    }
    def pftLiner = (fstr, s) => liner(fstr.replace(escape, s))            // when a pre-format is involved
    def enter    = (_   , s) => traceStack.push(s)                        // don't print when evaluation starts...
    def exit     = (fstr, s) => pftLiner(fstr, traceStack.pop + s)        // ...but print all when it completes
    
  } // trait SettingsByDefault 
  
} // package tracing 

  
  
import language.experimental.macros
import scala.reflect.macros.Context

package object tracing {

  /*
   *  MAIN ENTRY POINTS
   */
 
  // Line traces are not bound to expressions
  def |--:()= $LT.NoLineTracer  // a disabled line trace
  def |++:()= $LT.LineTracer    // an enabled line trace 
  
  // Special type that is implicitly converted to an expression tracer (the String type is also implicitly converted)
  type $e = $BEXViewer

  
  /*
   *  SHORTCUTS 
   */

  def ?() = org.stellabs.scart.config.Settings;   // A shortcut to the settings
  def %() = $ExpressionUtil                       // A shortcut to utilities
  def !!! = Abort                                 // A shortcut to the abortion mechanism
 

  /*
   *  EXPRESSION TRACERS
   */

  import scala.language.implicitConversions

  // Either the beginning, the end, or the thrown exception of an expression's evaluation
  sealed abstract class $BeginEndExcept[+B,+E,+X];
  final case class $B[+B,+E,+X](v:B) extends $BeginEndExcept[B,E,X]
  final case class $E[+B,+E,+X](v:E) extends $BeginEndExcept[B,E,X]
  final case class $X[+B,+E,+X](v:X) extends $BeginEndExcept[B,E,X]

  // A viewer's purpose is to provide different strings whether an expression's evaluation begins, ends or fails
  type $BEXViewer = $BeginEndExcept[_,_,_] => String

  // A partial viewer's purpose is to provide a string for only one case among: beginning, end, failure; as in the above
  type $BEXPartViewer = PartialFunction[$BeginEndExcept[_,_,_],String]


  // Expression tracers attempt or guarantee -depending on the usage- to trace:
  // - a string before the evaluation of an expression
  // - a string after the evaluation, based on its result
  // - a string if the evaluation fails, optionally based of the thrown exception's text
  // USAGE: the trace mechanisms rely on right-associative, lowest priority, operators.
  // - used as operators as in `expression operator traceData` traces are tentatively performed
  // - used as methods as in `traceData.method(expression)` traces shall be performed
  // IMPORTANT NOTE: the compiler may generate code using a synthetic intermediate variable to store the expression then
  // pass it to the operator. If an exception is thrown when the expression is evaluated in that case, with the 1st form
  // the operator is NOT processed at all. However the 2nd form gets the opportunity to process the operator, because
  // that occurs in its scope (both forms are implemented using macros). Hence the attempt/guarantee difference.
  // HOWEVER, the 1st is less invasive as it can just be appended to the existing source code, most of the time.
  object $ET{
    class ExprTracer(val viewer:$BEXViewer = null, val preformat:String = null){
      
      // A disabled trace just returns the expression
      def e_--:[E](_expression: =>E):E = macro `itself(E):E`[E]
      def x_--:[E](_expression: =>E):E = macro `itself(E):E`[E]
            
      // Trace the evaluation of a Scala expression
      // - before eval      : invoke the handler that traces the beginning of an expression, with () as argument to view
      // - after eval       : ditto for the end, with the evaluation's result as argument
      // - in case of error : ditto for the failure, with the Throwable as argument (NOTE: it's caught and rethrown thus
      //   in the exception stack, expressions line# can be replaced with traces'. That's a limitation.)
      // Handlers receive respective strings yielded by respective viewers
      def e_++:[E](expression: =>E):E = {
        ?.enter(preformat, viewer($B(null)))
        try{ val e = expression; ?.exit(preformat, viewer($E(e))); e
        } catch{ case t:java.lang.Throwable => ?.exit( preformat, viewer($X(t)) ); throw t }
      }
      
      
      // Same as above but if an error occurs, no exception is caught: a null Throwable as argument to view.
      // That avoids the line# redirection, however the exception contents can't be inspected. 
      def x_++:[E](expression: =>E):E = {
        var error = true; ?.enter(preformat, viewer($B(null))) 
        try{ val e = expression; error=false; ?.exit( preformat, viewer($E(e)) ); e
        } finally if (error){ ?.exit( preformat, viewer($X(null:java.lang.Throwable)) ) }
      }
    } // ExprTracer
    

    // A tracer that never traces
    final object NoExprTracer extends ExprTracer {
      override def e_++:[E](_expression: =>E):E = macro `itself(E):E`[E]
      override def x_++:[E](_expression: =>E):E = macro `itself(E):E`[E]
    }
    
    
    /* MACRO IMPLEMENTATIONS */

    // Just yield the designated expression
    def `itself(E):E`[E](c:Context)(_expression:c.Expr[E]):c.Expr[E] = _expression

  } //$ET
  
  
  /*
   *  IMPLICIT CONVERTERS AND THEIR MACRO IMPLEMENTATIONS
   *  NOTE: the following compiler warnings can be ignored:
   *        - Class org.stellabs.scart.tracing.package$$LT not found - continuing with a stub.
   *        - Class org.stellabs.scart.tracing.package$$ET not found - continuing with a stub.
   */

  // A mechanism to implicitly create, given a viewer, a tracer object able to handle the beginning, end or failure of
  // expression evaluation
  implicit def $Viewer2ET(_viewer:$BEXViewer):$ET.ExprTracer = macro `Viewer2ET($BEXViewer):$ET.ExprTracer`
  def `Viewer2ET($BEXViewer):$ET.ExprTracer`(c:Context)(_viewer:c.Expr[$BEXViewer]):c.Expr[$ET.ExprTracer] =
    if (?.doIt) %._newETFromViewer(c)(_viewer) else c.universe.reify{ $ET.NoExprTracer }

  // Ditto, but given a String instead. A basic viewer is made from it.
  implicit def $String2ET(_entry:String):$ET.ExprTracer = macro `String2ET(String):$ET.ExprTracer`
  def `String2ET(String):$ET.ExprTracer`(c:Context)(_entry:c.Expr[String]):c.Expr[$ET.ExprTracer] ={ import c.universe._
    if (?.doIt) %._newETFromViewer(c)(reify{ ?.bexViewer(_entry.splice) }) else reify{ $ET.NoExprTracer }
  }
  
  // Ditto, but given a Symbol instead. A basic viewer is made from it.
  implicit def $Symbol2ET(_entry:Symbol):$ET.ExprTracer = macro `Symbol2ET(Symbol):$ET.ExprTracer`
  def `Symbol2ET(Symbol):$ET.ExprTracer`(c:Context)(_entry:c.Expr[Symbol]):c.Expr[$ET.ExprTracer] ={ import c.universe._
    if (?.doIt) %._newETFromViewer(c)(reify{ ?.bexViewer(_entry.splice.name) }) else reify{ $ET.NoExprTracer }
  }

  // Ditto, but given a pair, with 1st element: a trace filtering literal, and 2nd: a type to convert
  implicit def $Viewer2ET[V<:AnyVal,FROMTYPE](_pair:Tuple2[V,FROMTYPE]):$ET.ExprTracer =
    macro `Viewer2ET(V->String):$ET.ExprTracer`[V,FROMTYPE]
  def `Viewer2ET(V->String):$ET.ExprTracer`[V:c.WeakTypeTag,FROMTYPE:c.WeakTypeTag](c:Context)
                (_pair:c.Expr[Tuple2[V,FROMTYPE]]):c.Expr[$ET.ExprTracer] =
  if (?.doIt) {
    import c.universe._; implicit val k = c
    def value[T1<:AnyVal,T2]:T1 = _pair.tree match{
      // Used arrow to construct the pair
      case Apply(TypeApply(Select(Apply(_:TypeApply, List(Literal(Constant(v)))), _), _), _)  => v.asInstanceOf[T1]
      // Used tuple parens to construct the pair
      case Apply(_:TypeApply, List(Literal(Constant(v)), _))                                  => v.asInstanceOf[T1]
      // In the event that the compiler yields another kind of AST: evaluate; however that raises a warning message
      case pvt => try{ c.eval(c.Expr[Tuple2[T1,T2]](c.resetAllAttrs(pvt.duplicate)))._1
                  } catch{ case _:java.lang.Throwable => !!!.nonLiteralValue }
    }
    // If the pair's 1st element is a Int, handle as the trace priority; if it's a Boolean, as a local trace switch.
    // The 2nd element, also, is either a viewer or a string that must be transformed to a viewer.
    val ViewerTpe = c.weakTypeOf[$BEXViewer]; val StringTpe = c.weakTypeOf[String]
    (c.weakTypeOf[V], c.weakTypeOf[FROMTYPE]) match{
      case (definitions.IntTpe, fromTpe) if fromTpe =:= ViewerTpe     =>  val prio = value[Int,$BEXViewer]
        if (?.priority >= prio) %._newETFromViewerPair(c)(_pair.asInstanceOf[c.Expr[Tuple2[Int,$BEXViewer]]])
        else reify{ $ET.NoExprTracer }
      case (definitions.BooleanTpe, fromTpe) if fromTpe =:= ViewerTpe =>  val cond = value[Boolean,$BEXViewer]
        if (cond) %._newETFromViewerPair(c)(_pair.asInstanceOf[c.Expr[Tuple2[Boolean,$BEXViewer]]])
          else reify{ $ET.NoExprTracer }
      case (definitions.IntTpe, fromTpe) if fromTpe =:= StringTpe     =>  val prio = value[Int,String]
        if (?.priority >= prio) %._newETFromStringPair(c)(_pair.asInstanceOf[c.Expr[Tuple2[Int,String]]])
        else reify{ $ET.NoExprTracer }
      case (definitions.BooleanTpe, fromTpe) if fromTpe =:= StringTpe =>  val cond = value[Boolean,String]
        if (cond) %._newETFromStringPair(c)(_pair.asInstanceOf[c.Expr[Tuple2[Boolean,String]]])
        else reify{ $ET.NoExprTracer }
      case _                                    =>  !!!.invalidPair                                
    }
  } else c.universe.reify{ $ET.NoExprTracer }    


  /*
   *  LINE TRACERS
   */
   
  object $LT{
     
    // A trace that, by default, ignores the trace requests (abstract)
    protected abstract class Base{
      def $():Unit                = macro `donttrace():Unit`
      def $_(_entry:String):Unit  = macro `donttrace(String):Unit`
      def $$(_entry:String):Unit  = macro `donttrace(String):Unit`  
      def %(_priority:Int):Base   = macro `nevertracer(Int):Base`
      def %(_cond:Boolean):Base   = macro `nevertracer(Boolean):Base`
    }
  
    // A "do-nothing" entry-point
    final object NoLineTracer extends Base;
  
    // A trace entry-point that traces, or not, depending on the settings
    final object LineTracer extends Base{
      override def $():Unit               = macro `fasttrace():Unit`
      override def $_(_entry:String):Unit = macro `fasttrace(String):Unit`
      override def $$(_entry:String):Unit = macro `trace(String):Unit`
      override def %(_priority:Int):Base  = macro `priotrace(Int):Base`
      override def %(_cond:Boolean):Base  = macro `priotrace(Boolean):Base`
    }
  
    
    /* MACRO IMPLEMENTATIONS */
     
    // Redirect to the "do-nothing" tracer if and only if the condition is false
  	def `priotrace(Boolean):Base` (c:Context)(_cond:c.Expr[Boolean]):c.Expr[Base] ={ import c.universe._
  	  _cond.tree match {
  	    case Literal(Constant(cond:Boolean))  => if (cond) reify{ $LT.LineTracer } else reify{ $LT.NoLineTracer}
  	    case _                                => !!!.nonLiteralCondition(c)
  	} }
  	
    // Redirect to the "do-nothing" tracer if a trace's priority Int is bigger than specified in the settings
  	def `priotrace(Int):Base` (c:Context)(_priority:c.Expr[Int]):c.Expr[Base] ={ import c.universe._
  	  _priority.tree match {
  	    case Literal(Constant(prio:Int)) => if(?.priority >= prio) reify{ $LT.LineTracer } else reify{ $LT.NoLineTracer}
  	    case _                            => !!!.nonLiteralPriority(c)
  	} }
  
    	
    // Ignore any trace request, evaluate to {}
    def `donttrace():Unit`(c:Context)():c.Expr[Unit]                            = %._void(c)
    def `donttrace(String):Unit`(c:Context)(_entry:c.Expr[String]):c.Expr[Unit] = %._void(c)
  
    def `nevertracer(Int):Base`(c:Context)(_priority:c.Expr[Int]):c.Expr[Base]    = c.universe.reify{ $LT.NoLineTracer }
    def `nevertracer(Boolean):Base`(c:Context)(_cond:c.Expr[Boolean]):c.Expr[Base]= c.universe.reify{ $LT.NoLineTracer }
  
  
    // Gather the additional data that is useful for the trace, use it to pre-format the entry
    def `trace(String):Unit` (c:Context)(_entry:c.Expr[String]):c.Expr[Unit] =
      if (?.doIt) %._preformatLiner(c)(_entry) else %._void(c)
  
  
    // Ditto but for literal strings; save some steps and precious ticks
    def `fasttrace(String):Unit` (c:Context)(_entry:c.Expr[String]):c.Expr[Unit]={
      import c.universe._; implicit val k = c
      if (?.doIt) _entry.tree match {
        case Literal(Constant(entry:String))  => val _pftstr = %._preformat(entry); reify{ ?.liner(_pftstr.splice) }
        case _                                => !!!.nonLiteralEntry
      } else reify{ () }
    }
  
    // Ditto but without a string entry
    def `fasttrace():Unit` (c:Context)():c.Expr[Unit]={ import c.universe._; implicit val k = c
      if (?.doIt){ val _pftstr = %._preformat(null); reify{ ?.liner(_pftstr.splice) } } 
      else reify{ () }
    }
    
  } //$LT
  
  
  /*
   *  EXPRESSION TYPES TRACERS
   */

  object $TT{
    
    // Helper to show an expression type within a Line or Expression Tracer PROVIDED that it has been evaluated ALREADY
    // NOTE that means: - it doesn't work in the Tracer of the expression that's being evaluated! (weird string appears)
    //                  - it MUST NOT be used on a yet-to-evaluate one (e.g. lazy: risk to change the evaluation order)
    // That contains a triplet of Strings that are several ways to represent the same type
    class $TypeViewer(val $3t:Tuple3[String,String,String]) extends AnyVal {
      def $rt = $3t._1  // raw type
      def $nt = $3t._2  // normalized type
      def $wt = $3t._3  // widen type
    }
    
    def `Expr2$TypeViewer`[E:c.WeakTypeTag](c:Context)(_expression:c.Expr[E]):c.Expr[$TypeViewer] ={ import c.universe._
      val wt = c.weakTypeOf[E]
      val _ts1 = c.literal(s"${wt}")
      val _ts2 = c.literal(s"${wt.normalize}")
      val _ts3 = c.literal(s"${wt.widen}")
      reify{ new $TypeViewer((_ts1.splice, _ts2.splice, _ts3.splice)) }
    }
    
  } // $TT

  implicit def $Expr2TypeViewer[E](_expression:E):$TT.$TypeViewer = macro $TT.`Expr2$TypeViewer`[E]


  object $ExpressionUtil{
    
    // Yield an empty Unit expression
    def _void(implicit k:Context):k.Expr[Unit] = k.universe.reify{ () }
      
    // Yield a line printer for the entry with a pre-formatted string that contains additional trace info
    def _preformatLiner(c:Context)(_entry:c.Expr[String]):c.Expr[Unit] = { import c.universe._; implicit val k = c
      val _pftstr = _preformat(?.escape)
      reify{ ?.pftLiner( _pftstr.splice, _entry.splice ) }
    }
    
    
    def _preformat(entry:String)(implicit k:Context) =
      k.literal( ?.formatter( TraceData(entry, currentInclosure, currentLocation) ) )
    
  	
    // Yield an expression that instantiates an Expression Tracer from a viewer
    def _newETFromViewer(c:Context)(_viewer:c.Expr[$BEXViewer]) = { import c.universe._; implicit val k = c
      val _pftstr = _preformat(?.escape)
      reify{ new $ET.ExprTracer( _viewer.splice, _pftstr.splice) }
    }

    // Yield an expression that instantiates an Expression Tracer from a (_, viewer) pair
    def _newETFromViewerPair(c:Context)(_pair:c.Expr[Tuple2[_,$BEXViewer]]) = { import c.universe._; implicit val k = c
      val _pftstr = %._preformat(?.escape)
      reify{ new $ET.ExprTracer( _pair.splice._2, _pftstr.splice) }
    }

    // Ditto from a (_, string) pair
    def _newETFromStringPair(c:Context)(_pair:c.Expr[Tuple2[_,String]]) =
    { _newETFromViewer(c)( c.universe.reify{ ?.bexViewer(_pair.splice._2) } ) }


    /* YET UNUSED */
    
    // Yield a literal string printer
    def _print(c:Context)(_entry:c.Expr[String]):c.Expr[Unit] = { import c.universe._; implicit val k = c
      _entry.tree match {
        case Literal(Constant(entry:String))  => val _pftstr = _preformat(entry); reify{ ?.liner( _pftstr.splice ) }
        case _                                => !!!.nonLiteralEntry
    } }

    
    def doItUponPriority(c:Context)(_priority:c.Expr[Int]):Boolean = { import c.universe._
  		_priority.tree match{
  		  case Literal(Constant(prio:Int)) => ?.doIt && ?.priority >= prio // the boolean result
  		  case _ => !!!.nonLiteralPriority(c)
  		}
  	}	
 
  } //ExpressionUtil

  
  /*
   * TRACE DATA
   */

  // Taxonomy of the current method, class, as specific as possible
  object InclosureKind extends Enumeration { val UNKNOWN, CLASS, METHOD = Value }
  case class Inclosure(kind:InclosureKind.Value, id:String){
    def this(v:Int,s:String) = this( InclosureKind(v), s );
  }
  
  // Location in the source code
  case class Location(source:String, val line:Int, val column:Int){
    def this(t:(String,Int,Int)) = this( if(null==t) null else t._1, if(null==t) 0 else t._2, if(null==t) 0 else t._3 );
  }
  
  // Combination of the above, with the trace's entry string
  case class TraceData(entry:String, member:Inclosure, loc:Location);

  
  /*
   * FEATURES USED IN THE ABOVE, BUT CAN ALSO BE USEFUL ELSEWHERE
   */

  // Determine the position in the source code: Filename, Line, Column
  def $currentPosition:(String,Int,Int) = macro `currentPosition:(String,Int,Int)`;
  def `currentPosition:(String,Int,Int)` (c:Context):c.Expr[(String,Int,Int)]={ import c.universe._; implicit val k = c
    currentPosition match {
      case (path, line, column) => val (_path, _line, _column) = (c.literal(path), c.literal(line), c.literal(column))
                                   reify{ (_path.splice, _line.splice, _column.splice) }
      case _                    => reify{ (null,0,0) }
  } }
  
  private def currentPosition(implicit k:Context) = {
    val ep = k.enclosingPosition
    if (null==ep || !ep.isDefined) null
    else{
      val path = (new java.io.File(ep.source.path)).getCanonicalPath
      val relativizer = ?.relativizer
      (if (null==relativizer) path else relativize(path, relativizer), ep.line, ep.column)
    }
  }

  
  // Determine the location in the source code (similar to position): Filename, Line, Column
  def $currentLocation:tracing.Location = macro `currentLocation:Location`;
  def `currentLocation:Location` (c:Context):c.Expr[tracing.Location]= c.universe.reify{
    new Location( `currentPosition:(String,Int,Int)`(c).splice )
  }
  
  private def currentLocation(implicit k:Context) = new Location(currentPosition);


  // Identify the class or method to which the source code being executed belongs
  def $currentInclosure:tracing.Inclosure = macro `currentInclosure:Inclosure`;
  def `currentInclosure:Inclosure` (c:Context):c.Expr[tracing.Inclosure]= {
    val Inclosure(kind, id) = currentInclosure(c)
    c.universe.reify{ new Inclosure( c.literal(kind.id).splice, c.literal(id).splice ) }
  }
  def currentInclosure(implicit k:Context) = 
    if (null != k.enclosingMethod && !k.enclosingMethod.isEmpty) 
      Inclosure( InclosureKind.METHOD, k.enclosingMethod.symbol.fullName )
    else if (null != k.enclosingClass && !k.enclosingClass.isEmpty) 
      Inclosure( InclosureKind.CLASS, k.enclosingClass.symbol.fullName )
    else Inclosure( InclosureKind.UNKNOWN , "<???>" )
  
  
  /*
   * FEATURES UNUSED IN THE ABOVE, BUT THAT ALSO BE USEFUL ELSEWHERE
   */
  
  // Not as flexible as reflection, but many times faster when that is applicable: class name     
  def $currentClassName:String = macro `currentClassName:String`;
  def `currentClassName:String` (c:Context):c.Expr[String]={
    import c.universe._;
    c.Expr(Literal(Constant(c.enclosingClass.symbol.fullName)))
  }

  // Not as flexible as reflection, but many times faster when that is applicable: method name
  // Limitation: it doesn't give an outer method's inner method name
  def $currentMethodName:String = macro `currentMethodName:String`;
  def `currentMethodName:String` (c:Context):c.Expr[String]= c.literal( c.enclosingMethod.symbol.fullName )

  
  /*
   *  ERRORS during macro evaluation at user app's compile-time
   */
   
  // Abort macro evaluation and provide details the cause
  final object Abort{
    def apply(desc:String, hint:String = null)(implicit k:Context) =
      k.abort(k.enclosingPosition, "Trace Macro Error: " + (if (null==hint) desc else s"${desc} (${hint})"));
    def nonLiteralEntry(implicit k:Context) =
      apply("invalid Entry", "possible cause: mistakenly using a non-literal String")
    def nonLiteralCondition(implicit k:Context) =
      apply("invalid Condition", "possible cause: mistakenly using a non-literal Boolean")
    def nonLiteralPriority(implicit k:Context) = 
      apply ("invalid Priority", "possible cause: mistakenly using a non-literal Int")
    def nonLiteralValue(implicit k:Context) = 
      apply ("invalid Priority", "possible cause: mistakenly using a non-literal AnyVal")
    def invalidPair(implicit k:Context) = 
      apply ("invalid Pair", "possible cause: mistakenly using wrong combination of types or elements")

  }


  /*
   *  OTHER UTILITIES
   */

  // Relativize an absolute and canonical path (source) against another (origin), if possible
  // => the relative path from origin to source, or the source path as-is if undoable (e.g. source home is opaque)
  // NOTE: the sole purpose is displaying (result won't work for Windows to access files)
  private def relativize(absSrc:String, absOri:String) = {
    // Get the separate dir names except the root's (done via URI to harmonize the Windows format with POSIX)
    val aSrc = (new java.io.File(absSrc)).toURI.getPath.split("/").tail
    val aOri = (new java.io.File(absOri)).toURI.getPath.split("/").tail
      
    // Split the paths differences into left:'from-origin-to-common-dir' & right:'from-common-dir-to-source'
    val dSz  = aSrc.size - aOri.size
    val diff = {
      { if      (0==dSz){ aSrc zip aOri }
        else if (0<dSz) { aSrc zip aOri ++ Array.ofDim[String](dSz) }
        else            { aSrc ++ Array.ofDim[String](-dSz) zip aOri }
      } dropWhile{ case (name1, name2) => name1 == name2
      } map { case(n,null) => (null,n); case(n,_) => ("..",n) }
    }.unzip
    
    // Yield the relative path string unless there isn't a common dir between in the source and the origin
    val prefixes = diff._1 filter{ null!=_ }
    val suffixes = diff._2 filter{ null!=_ }
    prefixes.size match{
      case sz if sz==aOri.size  => absSrc // Return source path as-is because of opacity: root might not be system-root
      case 0                    => if (0==suffixes.size) "." else "./" + suffixes.mkString("/")
      case _                    => prefixes.mkString("/")+(if (0==suffixes.size) "" else ("/" + suffixes.mkString("/")))
    }
  }
  
   
} // package object tracing

