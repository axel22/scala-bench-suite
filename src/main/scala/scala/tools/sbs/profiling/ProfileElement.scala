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

  def toXML =
    <class>
      <name>{ name }</name>
      <fields>{ for (field <- fields) yield field.toXML }</fields>
      <methods>{ for (method <- methodInvoked) yield method.toXML }</methods>
    </class>
}

/** A benchmark method that has been invoked in a benchmark run.
 *
 *  The name should be <method name>.<argument types>.<return type>.
 */
case class InvokedMethod(name: String) extends ProfileElement {

  case class Invocation(steps: Int)

  /** Number of all the invocations of this method.
   */
  private var _invocations = ArrayBuffer[Invocation]()

  def invocations = _invocations

  def hasInvoked(steps: Int) {
    _invocations += Invocation(steps)
  }

  def toXML =
    <method>
      <name>{ name }</name>
      <invocations>{ for (invoked <- invocations) yield <invoked>{ invoked.steps }</invoked> }</invocations>
    </method>
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

  def toXML =
    <field>
      <name>{ name }</name>
      <accessed>{ accessed }</accessed>
      <modified>{ modified }</modified>
    </field>
}

/** A garbage collector's running profile.
 */
case class GarbageCollection(name: String, cycle: Int, totalTime: Int) {

  def toXML =
    <gc>
      <name>{ name }</name>
      <cycle>{ cycle }</cycle>
      <time>{ totalTime }</time>
    </gc>

}

