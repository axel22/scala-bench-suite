/*
 * InvocationGraph
 * 
 * Version
 * 
 * Created on Novemeber 23rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package bottleneck

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor

/** Represents a list of method call expressions inside a method body in order of time.
 */
case class InvocationGraph(methods: ArrayBuffer[MethodCall],
                           steps: ArrayBuffer[Step],
                           startOrdinum: Int,
                           private var _endOrdinum: Int) {

  def this() = this(ArrayBuffer[MethodCall](), ArrayBuffer[Step](), 0, 0)

  /** Traverses through all method call expressions in order of time.
   */
  def traverse(operate: MethodCall => Any) = {
    operate(first) +: (steps map (step => operate(step.to)))
  }

  /** The ordinal number of the invocation of the method called at the end
   *  of this graph.
   */
  def endOrdinum: Int = _endOrdinum

  /** Number of method call expressions.
   */
  def length: Int = if (steps isEmpty) methods.length else steps.length + 1

  /** The oldest method call expression in respect of time orders.
   */
  def first: MethodCall = if (steps isEmpty) if (methods isEmpty) null else methods.head else steps.head.from

  /** The latest method call expression in respect of time orders.
   */
  def last: MethodCall = if (steps isEmpty) if (methods isEmpty) null else methods.last else steps.last.to

  /** Adds new method call expression into this graph.
   *
   *  @param	prottoype	Name and signature of the method has just been called.
   */
  def add(declaringClass: String, methodName: String, signature: String) {
    def addStep(to: MethodCall) {
      val from = if (steps isEmpty) methods.head else steps.last.to
      steps append Step(from, from.timesCalled, to, to.timesCalled)
    }
    val newCall = MethodCall(declaringClass, methodName, signature)
    methods find (_.prototype == newCall.prototype) match {
      case Some(existed) =>
        existed.calledAgain
        addStep(existed)
      case None =>
        methods append newCall
        if (methods.length > 1) addStep(methods.last)
    }
    _endOrdinum = if (steps isEmpty) 1 else steps.last.toOrdinum
  }

  /** Splits this graph into two new graphs which have equivalent length
   *  in respect of time orders.
   */
  def split: (InvocationGraph, InvocationGraph) =
    if (length < 2) throw new Error("Should not split anymore.")
    else {
      def attendIn(stepList: ArrayBuffer[Step]) =
        if (stepList isEmpty) None
        else Some(methods filter (i => stepList exists (s => s.from == i || s.to == i)))

      val break = steps.length / 2

      val firstSteps = steps take break
      val secondSteps = steps takeRight (steps.length - break - 1)
      val firstInvocations = attendIn(firstSteps) getOrElse ArrayBuffer(first)
      val secondInvocations = attendIn(secondSteps) getOrElse ArrayBuffer(last)
      (new InvocationGraph(firstInvocations, firstSteps, startOrdinum, steps(break).fromOrdinum),
        new InvocationGraph(secondInvocations, secondSteps, steps(break).toOrdinum, endOrdinum))
    }

  /** Checks whether this graph represents the same invocation list
   *  with the given one.
   */
  def matches(that: InvocationGraph): Boolean =
    (startOrdinum == that.startOrdinum) &&
      (endOrdinum == that.endOrdinum) &&
      ((methods map (_ prototype)) == (that.methods map (_ prototype))) &&
      ((methods map (_ timesCalled)) == (that.methods map (_ timesCalled))) &&
      ((steps map (_.from.prototype)) == (that.steps map (_.from.prototype))) &&
      ((steps map (_.to.prototype)) == (that.steps map (_.to.prototype))) &&
      ((steps map (_ fromOrdinum)) == (that.steps map (_ fromOrdinum))) &&
      ((steps map (_ toOrdinum)) == (that.steps map (_ toOrdinum)))

}

/** Method call expression - node of the graph.
 */
case class MethodCall(declaringClass: String, methodName: String, signature: String) {

  private var _timesCalled = 1

  /** Number of times this method call expression has been run.
   */
  def timesCalled = _timesCalled

  /** This method should be called whenever the represented
   *  method call expression is run.
   */
  def calledAgain = _timesCalled += 1

  /** Call expression identifier.
   */
  val prototype: String = CodeInstrumentor.prototype(declaringClass, methodName, signature)

}

/** Vertex of the graph.
 */
case class Step(from: MethodCall, fromOrdinum: Int, to: MethodCall, toOrdinum: Int)
