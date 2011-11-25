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

/** Represents a list of method call expressions inside a method body in order of time.
 */
class InvocationGraph(invocations: ArrayBuffer[Invocation], steps: ArrayBuffer[Step]) {

  def this() = this(ArrayBuffer[Invocation](), ArrayBuffer[Step]())

  /** Traverses through all method call expressions in order of time.
   */
  def traverse(operate: Invocation => Unit): Unit = {
    operate(first)
    steps foreach (step => operate(step.to))
  }

  /** Number of method call expressions.
   */
  def length: Int = if (steps isEmpty) invocations.length else steps.length + 1

  /** The oldest method call expression in respect of time orders.
   */
  def first: Invocation = if (steps isEmpty) if (invocations isEmpty) null else invocations.head else steps.head.from

  /** The latest method call expression in respect of time orders.
   */
  def last: Invocation = if (steps isEmpty) if (invocations isEmpty) null else invocations.last else steps.last.to

  /** Adds new method call expression into this graph.
   *
   *  @param	prottoype	Name and signature of the method has just been called.
   */
  def add(prototype: String): Unit = {
    def addStep(to: Invocation) = steps append Step(if (steps isEmpty) invocations.head else steps.last.to, to)
    invocations find (_.prototype == prototype) match {
      case Some(existed) =>
        addStep(existed)
      case None =>
        invocations append Invocation(prototype)
        if (invocations.length > 1) addStep(invocations.last)
    }
  }

  /** Splits this graph into two new graphs which have equivalent length
   *  in respect of time orders.
   */
  def split: (InvocationGraph, InvocationGraph) =
    if (length < 2) throw new Error("Should not split anymore.")
    else {
      def attendIn(stepList: ArrayBuffer[Step]) =
        if (stepList isEmpty) None
        else Some(invocations filter (i => stepList exists (s => s.from == i || s.to == i)))

      val break = steps.length / 2

      val firstSteps = steps take break
      val secondSteps = steps takeRight (steps.length - break - 1)
      val firstInvocations = attendIn(firstSteps) getOrElse ArrayBuffer(first)
      val secondInvocations = attendIn(secondSteps) getOrElse ArrayBuffer(last)
      (new InvocationGraph(firstInvocations, firstSteps), new InvocationGraph(secondInvocations, secondSteps))
    }

}

case class Invocation(prototype: String)

case class Step(from: Invocation, to: Invocation)
