/*
 * Type
 * 
 * Version
 * 
 * Created on October 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.collection.mutable.ArrayBuffer

trait ProfileElement {

  def name: String

}

/** A benchmark's class that has been loaded in a benchmark run.
 */
case class LoadedClass(name: String) extends ProfileElement {

  /** All fields of the class that has been accessed or modified
   *  during the benchmark run.
   */
  private var _fields = ArrayBuffer[Field]()

  def fields = _fields

  def addField(field: Field) {
    _fields += field
  }

  /** All methods of the class that has been invoked
   *  during the benchmark run.
   */
  private var _methodInvoked = ArrayBuffer[InvokedMethod]()

  def methodInvoked = _methodInvoked

  def invokeMethod(method: InvokedMethod) {
    _methodInvoked += method
  }

}

/** A benchmark method that has been invoked in a benchmark run.
 *
 *  The name should be <method name>.<argument types>.<return type>.
 */
case class InvokedMethod(name: String) extends ProfileElement {

  case class Invocation(time: Long)

  /** Number of all the invocations of this method.
   */
  private var _invocations = ArrayBuffer[Invocation]()

  def invocations = _invocations

  def hasInvoked(time: Long) {
    _invocations += Invocation(time)
  }

}

/** A member value/variable that has been accessed or modified
 *  in a benchmark run.
 */
case class Field(name: String) extends ProfileElement {

  private var _accessed = 0

  def accessed = _accessed

  def access {
    _accessed += 1
  }

  private var _modified = 0

  def modified = _modified

  def modify {
    _modified += 1
  }

}
