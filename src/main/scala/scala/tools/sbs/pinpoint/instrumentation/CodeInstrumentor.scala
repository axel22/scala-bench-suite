/*
 * CodeInstrumentor
 * 
 * Version
 * 
 * Created on October 13th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package instrumentation

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.Instruction
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.InstrumentingClass
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.InstrumentingExpression
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.InstrumentingMethod
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression

trait CodeInstrumentor {

  /** Returns {@link scala.tools.sbs.pinpoint.InstrumentingClass} for `className`.
   */
  def getClass(className: String, classpathURLs: List[URL]): InstrumentingClass

  /** Searches for the method with the given name, declared in `clazz`.
   *  Return `null` if cannot be found.
   */
  def getMethod(clazz: InstrumentingClass, methodName: String): InstrumentingMethod

  /** Searches for the method with the given name, declared in the class with given `className`.
   *  Return `null` if cannot be found.
   */
  def getMethod(methodName: String, className: String, classpathURL: List[URL]): InstrumentingMethod

  /** Returns the method and its declaring class if exist.
   *  First, searches for the method in class `className`.
   *  If the method is not there, does the searching in
   *  class `className$` and `className$class`.
   *  Returns `null` for items that could not be found.
   */
  def getClassAndMethod(
    className: String, methodName: String, classpathURLs: List[URL]): (InstrumentingClass, InstrumentingMethod)

  /** Searches for method calles in the body of the given {@link InstrumentingMethod}.
   */
  def callListOf(method: InstrumentingMethod): List[MethodCallExpression]

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  to the given method at the start of line number `index`.
   */
  def inject(method: InstrumentingMethod, index: Int, instruction: Instruction)

  /** Inserts `upper` at the start and `lower` at the end of `method`.
   */
  def sandwich(method: InstrumentingMethod, upper: Instruction, lower: Instruction)

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  before the given `InstrumentingExpression`.
   */
  def insertBefore(expression: InstrumentingExpression, instruction: Instruction)

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  after the given `InstrumentingExpression`.
   */
  def insertAfter(expression: InstrumentingExpression, instruction: Instruction)

  /** Inserts `upper` at the start and `lower` at the end of `expression`.
   */
  def sandwich(expression: InstrumentingExpression, upper: Instruction, lower: Instruction)

  /** Inserts `upper` at the start and `lower` at the end of method call list from
   *  `first` to `last` in `method`.
   */
  def sandwichCallList(method: InstrumentingMethod, first: Int, upper: Instruction, last: Int, lower: Instruction)

  /** Overwrites the .class file contains `clazz` with the instrumented `clazz`.
   *
   *  @param clazz The instrumented class
   *  @param context The class loader that loads `clazz`
   */
  def overwrite(clazz: InstrumentingClass, context: ClassLoader)

  /** Writes the instrumented `clazz` in to `location`.
   */
  def writeFile(clazz: InstrumentingClass, location: Directory)

}

object CodeInstrumentor {

  type Instruction = String
  type InstrumentingClass = javassist.CtClass
  type InstrumentingMethod = javassist.CtMethod
  type InstrumentingExpression = javassist.expr.Expr
  type MethodCallExpression = javassist.expr.MethodCall

  def apply(config: Config, log: Log, exclude: List[String]): CodeInstrumentor = {
    new JavassistCodeInstrumenter(config, log, exclude)
  }

}
