/*
 * SimpleReflector
 * 
 * Version
 * 
 * Created on October 15th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.sbs.benchmark.BenchmarkTemplate
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.COMPANION_FIELD
import scala.tools.sbs.util.Constant.DOLLAR

/** A simple implement of {@link Reflection}.
 */
class SimpleReflector(config: Config, log: Log) extends Reflector {

  def getClass(name: String, classpathURLs: List[URL]): Class[_] = {
    val classLoader = ScalaClassLoader.fromURLs(classpathURLs, classOf[BenchmarkTemplate].getClassLoader)
    classLoader tryToInitializeClass name getOrElse (throw new ClassNotFoundException(name))
  }

  def getObject[T](name: String, classpathURLs: List[URL]): T = {
    val clazz = getClass(name, classpathURLs)
    try {
      clazz.newInstance.asInstanceOf[T]
    }
    catch {
      case _: InstantiationException => {
        val clazz$ = getClass(name + DOLLAR, classpathURLs)
        (clazz$ getField COMPANION_FIELD get null).asInstanceOf[T]
      }
    }
  }

  def locationOf(name: String, classLoader: ClassLoader): Path = {
    try {
      val clazz = Class forName (name, false, classLoader)
      Path(clazz.getProtectionDomain.getCodeSource.getLocation.getPath).toCanonical
    }
    catch {
      case f: ClassNotFoundException => {
        UI.error("Class not found: " + name)
        log.debug("Class not found: " + name)
        throw f
      }
    }
  }

}
