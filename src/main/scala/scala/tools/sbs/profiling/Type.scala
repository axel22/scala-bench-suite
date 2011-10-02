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

trait Type {

  def name: String

}

/** A benchmark's class that has been loaded in a benchmark run.
 */
case class Class(name: String) extends Type {

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
  private var _methodInvoked = ArrayBuffer[Method]()

  def methodInvoked = _methodInvoked

  def addInvokedMethod(method: Method) {
    _methodInvoked += method
  }

}

/** A benchmark method that has been invoked in a benchmark run.
 *
 *  The name should be <method name>.<argument types>.<return type>.
 */
case class Method(name: String) extends Type {

  case class Invocation(stepPerformed: Int)

  /** Number of all the invocations of this method.
   */
  private var _invocations = ArrayBuffer[Invocation]()

  def invocations = _invocations

  def hasInvoked(stepPerformed: Int) {
    _invocations += Invocation(stepPerformed)
  }

}

/** A member value/variable that has been accessed or modified
 *  in a benchmark run.
 */
case class Field(name: String) extends Type {

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
