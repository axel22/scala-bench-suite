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
import scala.tools.sbs.measurement.MeasurementResult

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

  def toReport =
    ArrayBuffer("Loaded class " + name) ++ (fields flatMap (_.toReport)) ++ (methodInvoked flatMap (_.toReport))

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

  case class Invocation(steps: Int) {

    override def toString = "    Run for " + steps + " steps"

  }

  /** Number of all the invocations of this method.
   */
  private var _invocations = ArrayBuffer[Invocation]()

  def invocations = _invocations

  def hasInvoked(steps: Int) {
    _invocations += Invocation(steps)
  }

  def toReport = ArrayBuffer("  Invoked method: " + name) ++ (invocations map (_.toString))

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

  def toReport = ArrayBuffer("  Field: " + name, "    accessed " + accessed, "    modified " + modified)

  def toXML =
    <field>
      <name>{ name }</name>
      <accessed>{ accessed }</accessed>
      <modified>{ modified }</modified>
    </field>
}

/** A garbage collector's running profile.
 */
case class GarbageCollection(name: String, cycle: Long, totalTime: Long) {

  def toReport = ArrayBuffer("  Garbage collector: " + name, "    Cycle: " + cycle, "    Total time: " + totalTime)

  def toXML =
    <gc>
      <name>{ name }</name>
      <cycle>{ cycle }</cycle>
      <time>{ totalTime }</time>
    </gc>

}

case class MemoryUsage(init: Long, used: Long, committed: Long, max: Long) {

  def toReport = ArrayBuffer("Init: " + init, "Used: " + used, "Committed: " + committed, "Max: " + max)

  def toXML =
    <MemoryUsage>
      <init>{ init }</init>
      <used>{ used }</used>
      <committed>{ committed }</committed>
      <max>{ max }</max>
    </MemoryUsage>

}

case class MemoryActivity(heap: MemoryUsage, nonHeap: MemoryUsage) extends MeasurementResult {

  def this(heap: MemoryUsage, nonHeap: MemoryUsage, garbageCollections: Seq[GarbageCollection]) {
    this(heap, nonHeap)
    garbageCollections foreach (newGC => this.gc(newGC.name, newGC.cycle, newGC.totalTime))
  }

  /** Garbage collectors' cycles.
   */
  private val _garbageCollections = ArrayBuffer[GarbageCollection]()

  def garbageCollections = _garbageCollections

  def gc(name: String, cycle: Long, time: Long) {
    _garbageCollections += GarbageCollection(name, cycle, time)
  }

  def toReport =
    ArrayBuffer(
      "Memory activities: ",
      "Heap used: ") ++
      heap.toReport ++
      ArrayBuffer("Non heap used: ") ++
      nonHeap.toReport ++
      (garbageCollections flatMap (_.toReport))

  def toXML =
    <MemoryActivity>
      <heap>{ heap.toXML }</heap>
      <nonHeap>{ nonHeap.toXML }</nonHeap>
      <garbagecollection>{ for (gc <- garbageCollections) yield gc.toXML }</garbagecollection>
    </MemoryActivity>

}
