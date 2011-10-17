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

import java.net.URL

import scala.tools.sbs.Config

import CodeInstrumentor.Instruction
import CodeInstrumentor.InstrumentingClass
import CodeInstrumentor.InstrumentingMethod

trait CodeInstrumentor {

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  to the given method at the start of line number `index`.
   */
  def inject(index: Int, instruction: Instruction, method: InstrumentingMethod)

  /** Returns {@link scala.tools.sbs.pinpoint.InstrumentingClass} for `className`.
   */
  def getClass(className: String, classpathURLs: List[URL]): InstrumentingClass

  /** Searches for the method with the given name, declared in `clazz`.
   *  Return `null` if cannot be found.
   */
  def getMethod(clazz: InstrumentingClass, methodName: String): InstrumentingMethod

  /** Returns the method and its declaring class if exist.
   *  First, searches for the method in class `className`.
   *  If the method is not there, does the searching in
   *  class `className$` and `className$class`.
   *  Returns `null` for items that could not be found.
   */
  def getClassAndMethod(
    className: String, methodName: String, classpathURLs: List[URL]): (InstrumentingClass, InstrumentingMethod)

  /** Inserts `upper` at the start and `lower` at the end of `method`.
   */
  def sandwich(method: InstrumentingMethod, upper: Instruction, lower: Instruction)

  /** Overwritten the .class file contains `clazz` with the instrumented `clazz`.
   *
   *  @param clazz The instrumented class
   *  @param context The class loader that loads `clazz`
   */
  def writeFile(clazz: InstrumentingClass, context: ClassLoader)

}

object CodeInstrumentor {

  type Instruction = String
  type InstrumentingClass = javassist.CtClass
  type InstrumentingMethod = javassist.CtMethod

  def apply(config: Config): CodeInstrumentor = {
    new JavassistCodeInstrumenter(config)
  }

}
