/*
 * RuntimeTypeChecker
 * 
 * Version
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

trait RuntimeTypeChecker {

  /** `Manifest` of the expected benchmark type will be run by `this`.
   */
  protected val upperBound: Manifest[_]

  def check(clazz: Class[_]): Boolean = (Manifest classType clazz) <:< upperBound

}
