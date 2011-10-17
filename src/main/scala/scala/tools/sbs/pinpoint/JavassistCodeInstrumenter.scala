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

import java.net.URL

import scala.tools.sbs.common.Reflector
import scala.tools.sbs.Config

import CodeInstrumentor.Instruction
import CodeInstrumentor.InstrumentingClass
import CodeInstrumentor.InstrumentingMethod
import javassist.ClassPool

/** Uses javassist API to do instrumentation.
 */
class JavassistCodeInstrumenter(config: Config) extends CodeInstrumentor {

  def inject(index: Int, instruction: Instruction, method: InstrumentingMethod) {
    method.insertAt(index, instruction)
  }

  def getClass(className: String, classpathURLs: List[URL]): InstrumentingClass = {
    val classPool = ClassPool.getDefault
    classpathURLs foreach (cp => classPool appendClassPath cp.getPath)
    try {
      classPool get className
    }
    catch {
      case _: javassist.NotFoundException => null
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

  def sandwich(method: InstrumentingMethod, upper: Instruction, lower: Instruction): Unit = {
    method insertBefore upper
    method insertAfter lower
  }

  def writeFile(clazz: InstrumentingClass, context: ClassLoader) {
    clazz writeFile (Reflector(config).locationOf(clazz.getName, context) match {
      case Some(url) => url.getPath
      case None      => config.bin.path
    })
  }

}
