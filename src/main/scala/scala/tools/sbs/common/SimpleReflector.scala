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

import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.sbs.benchmark.BenchmarkTemplate
import scala.tools.sbs.util.Constant.DOLLAR
import scala.tools.sbs.util.Constant.COMPANION_FIELD

/** A simple implement of {@link Reflection}.
 */
class SimpleReflector(config: Config) extends Reflector {

  def getClass(name: String, classpathURLs: List[URL]): (Class[_], ClassLoader) = {
    val classLoader = ScalaClassLoader.fromURLs(classpathURLs, classOf[BenchmarkTemplate].getClassLoader)
    val clazz = classLoader tryToInitializeClass name getOrElse (throw new ClassNotFoundException(name))
    (clazz, classLoader)
  }

  def getObject[T](name: String, classpathURLs: List[URL]): (T, ClassLoader) = {
    val (clazz, classLoader) = getClass(name, classpathURLs)
    val obj =
      try {
        clazz.newInstance.asInstanceOf[T]
      }
      catch {
        case _: InstantiationException => {
          val clazz$ = getClass(name + DOLLAR, classpathURLs)._1
          (clazz$ getField COMPANION_FIELD get null).asInstanceOf[T]
        }
      }
    (obj, classLoader)
  }

  def locationOf(name: String, classLoader: ClassLoader): Option[URL] = {
    try {
      val clazz = Class forName (name, false, classLoader)
      Some(clazz.getProtectionDomain.getCodeSource.getLocation)
    }
    catch {
      case _: ClassNotFoundException => None
    }
  }

}
