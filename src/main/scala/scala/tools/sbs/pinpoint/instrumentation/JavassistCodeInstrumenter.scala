/*
 * JavassistCodeInstrumenter
 * 
 * Version
 * 
 * Created on October 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package instrumentation

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI

import CodeInstrumentor.Instruction
import CodeInstrumentor.InstrumentingClass
import CodeInstrumentor.InstrumentingExpression
import CodeInstrumentor.InstrumentingMethod
import CodeInstrumentor.MethodCallExpression

/** Uses javassist API to do instrumentation.
 */
class JavassistCodeInstrumenter(config: Config, log: Log, exclude: List[String]) extends CodeInstrumentor {

  private val proceedExpression: Instruction = "$_ = $proceed($$);"

  private def embrace(instruction: Instruction): Instruction = "{" + instruction + "}"

  def getClass(className: String, classpathURLs: List[URL]): InstrumentingClass = {
    val classPool = new javassist.ClassPool
    // Append these first to force javassist to load the right class from user classpath
    classpathURLs foreach (cp => classPool appendClassPath cp.getPath)
    classPool.appendSystemPath
    try {
      classPool get className
    }
    catch {
      case _: javassist.NotFoundException =>
        UI.debug("Class " + className + " cannot be found")
        log.debug("Class " + className + " cannot be found")
        null
    }
  }

  def getMethod(clazz: InstrumentingClass, methodName: String): InstrumentingMethod =
    try {
      clazz getDeclaredMethod methodName
    }
    catch {
      case _: javassist.NotFoundException => null
    }

  def getClassAndMethod(
    className: String, methodName: String, classpathURLs: List[URL]): (InstrumentingClass, InstrumentingMethod) = {
    val clazz$ = getClass(className + "$", classpathURLs)
    val clazz$class = getClass(className + "$class", classpathURLs)
    if ((clazz$ == null) && (clazz$class == null)) {
      val clazz = getClass(className, classpathURLs)
      if (clazz == null) {
        (null, null)
      }
      else {
        (clazz, getMethod(clazz, methodName))
      }
    }
    else if (clazz$ == null) {
      (clazz$class, getMethod(clazz$class, methodName))
    }
    else if (clazz$class == null) {
      (clazz$, getMethod(clazz$, methodName))
    }
    else {
      val clazz$Method = getMethod(clazz$, methodName)
      if (clazz$Method != null) {
        (clazz$, clazz$Method)
      }
      else {
        (clazz$class, getMethod(clazz$class, methodName))
      }
    }
  }

  def getMethod(methodName: String, className: String, classpathURLs: List[URL]): InstrumentingMethod =
    getClassAndMethod(className, methodName, classpathURLs) _2

  def callListOf(method: InstrumentingMethod): List[MethodCallExpression] = {
    var callList = List[MethodCallExpression]()
    method instrument new javassist.expr.ExprEditor {
      override def edit(call: MethodCallExpression) = {
        if (!exclude.exists(call.getClassName matches _)) {
          callList :+= call
          UI.debug("Method call collected: " + call.getClassName + "." + call.getMethodName)
          log.debug("Method call collected: " + call.getClassName + "." + call.getMethodName)
        }
      }
    }
    callList
  }

  def inject(method: InstrumentingMethod, index: Int, instruction: Instruction) =
    method.insertAt(index, instruction)

  def sandwich(method: InstrumentingMethod, upper: Instruction, lower: Instruction) {
    method insertBefore upper
    method insertAfter lower
  }

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  before the given `InstrumentingExpression`.
   */
  def insertBefore(expression: InstrumentingExpression, instruction: Instruction) {
    UI.debug("Insert before: " + embrace(instruction + proceedExpression))
    expression replace embrace(instruction + proceedExpression)
  }

  /** Inserts an {@link scala.tools.sbs.pinpoint.CodeInstrumenter.Instruction}
   *  after the given `InstrumentingExpression`.
   */
  def insertAfter(expression: InstrumentingExpression, instruction: Instruction) =
    expression replace embrace(proceedExpression + instruction)

  def sandwich(expression: InstrumentingExpression, upper: Instruction, lower: Instruction) {
    expression replace embrace(upper + proceedExpression + lower)
  }

  def sandwichCallList(method: InstrumentingMethod, first: Int, upper: Instruction, last: Int, lower: Instruction) {
    var index = 0
    method instrument new javassist.expr.ExprEditor {

      override def edit(call: MethodCallExpression) {
        if (!exclude.exists(call.getClassName matches _)) {

          UI.debug("Method call: " + call.getClassName + "." + call.getMethodName + call.getSignature)
          log.debug("Method call: " + call.getClassName + "." + call.getMethodName + call.getSignature)

          try if ((index == first) && (index == last)) {
            call replace embrace(upper + proceedExpression + lower)
          }
          else if (index == first) {
            call replace embrace(upper + proceedExpression)
          }
          else if (index == last) {
            call replace embrace(proceedExpression + lower)
          }
          catch {
            case e =>
              UI.error(e.toString)
              throw e
          }
          index += 1
        }
      }

    }
  }

  def overwrite(clazz: InstrumentingClass, context: ClassLoader) =
    clazz writeFile (Reflector(config, log).locationOf(clazz.getName, context).path)

  def writeFile(clazz: InstrumentingClass, location: Directory) =
    clazz writeFile location.path

}
