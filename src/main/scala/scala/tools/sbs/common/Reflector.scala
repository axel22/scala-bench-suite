/*
 * Reflector
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

/** All the reflecting stuff should be done here.
 */
trait Reflector {

  /** Loads the class with the given name in the given classpath.
   */
  def getClass(name: String, classpathURLs: List[URL]): (Class[_], ClassLoader)

  /** Gets the compinion object with the given name in the given classpath.
   */
  def getObject[T](name: String, classpathURLs: List[URL]): (T, ClassLoader)

  /** Gets the location where the class with given name was loaded.
   */
  def locationOf(name: String, classLoader: ClassLoader): Option[URL]

}

object Reflector {

  def apply(config: Config): Reflector = {
    new SimpleReflector(config)
  }

}
