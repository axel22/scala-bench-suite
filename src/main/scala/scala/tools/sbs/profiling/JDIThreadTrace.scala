/*
 * JDIThreadTrace
 * 
 * Version
 * 
 * Created on October 3rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.Stack
import scala.tools.sbs.io.Log

import com.sun.jdi.event.AccessWatchpointEvent
import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi.event.MethodEntryEvent
import com.sun.jdi.event.MethodExitEvent
import com.sun.jdi.event.ModificationWatchpointEvent
import com.sun.jdi.event.StepEvent
import com.sun.jdi.event.ThreadDeathEvent
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.StepRequest
import com.sun.jdi.ThreadReference
import com.sun.jdi.VirtualMachine

/** This class keeps context on events in one thread.
 */
class JDIThreadTrace(log: Log, profile: Profile, thread: ThreadReference, jvm: VirtualMachine) {

  /** Instruction steps of methods in call stack.
   */
  private var steps = Stack[Int]()

  /** Convert the `com.sun.jdi.Method` name into the format of <name>.<argument types>.<return type>
   */
  private def wrapName(method: com.sun.jdi.Method) =
    method.argumentTypeNames.asScala.toSeq.foldLeft(method.name + ".")((name, tpe) => name + tpe + ".") +
      method.returnTypeName

  /** Push new method on the call stack.
   */
  def methodEntryEvent(event: MethodEntryEvent) {
    steps push 0
    if (event.method.name startsWith "boxTo") {
      profile.box
    }
    if (event.method.name startsWith "UnboxTo") {
      profile.unbox
    }
  }

  /** Tries to update the method's invocation in profile.
   *  Adds the method and/or its declaring class into profile
   *  if not existed in.
   */
  def methodExitEvent(event: MethodExitEvent) {
    profile.classes find (_.name equals event.method.declaringType.name) match {
      case Some(clazz) => {
        clazz.methodInvoked find (_.name equals wrapName(event.method)) match {
          case Some(method) => {
            method.hasInvoked(steps pop)
          }
          case None => {
            val invoked = InvokedMethod(wrapName(event.method))
            invoked.hasInvoked(steps pop)
            clazz.invokeMethod(invoked)
          }
        }
      }
      case None => {
        val loaded = LoadedClass(event.method.declaringType.name)
        val invoked = InvokedMethod(wrapName(event.method))
        invoked.hasInvoked(steps pop)
        loaded.invokeMethod(invoked)
        profile.loadClass(loaded)
      }
    }
  }

  /** Actually do the accessing and/or modifying.
   */
  private def accessOrModify(event: com.sun.jdi.event.WatchpointEvent) {
    profile.classes find (_.name equals event.field.declaringType.name) match {
      case Some(clazz) => {
        clazz.fields find (_.name equals event.field.name) match {
          case Some(field) => event match {
            case _: AccessWatchpointEvent => field.access
            case _: ModificationWatchpointEvent => field.modify
          }
          case None => {
            val field = Field(event.field.name)
            event match {
              case _: AccessWatchpointEvent => field.access
              case _: ModificationWatchpointEvent => field.modify
            }
            clazz.addField(field)
          }
        }
      }

      case None => {
        val loaded = LoadedClass(event.field.declaringType.name)
        val field = Field(event.field.name)
        event match {
          case _: AccessWatchpointEvent => field.access
          case _: ModificationWatchpointEvent => field.modify
        }
        loaded.addField(field)
        profile.loadClass(loaded)
      }
    }
  }

  /** Tries to update the field's accessing times in profile.
   *  Adds the field and/or its declaring class into profile
   *  if not existed in.
   */
  def fieldAccessEvent(event: AccessWatchpointEvent) = accessOrModify(event)

  /** Tries to update the field's modifying times in profile.
   *  Adds the field and/or its declaring class into profile
   *  if not existed in.
   */
  def fieldModifyEvent(event: ModificationWatchpointEvent) = accessOrModify(event)

  def exceptionEvent(event: ExceptionEvent) {
    println("Exception: " + event.exception + " catch: " + event.catchLocation)

    // Step to the catch
    val mgr = jvm.eventRequestManager
    val req = mgr.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)
    req.addCountFilter(1) // next step only
    req.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    req.enable()
  }

  def stepEvent(event: StepEvent) {
    if (!steps.isEmpty) {
      steps push (steps.pop + 1)
    }
  }

  def threadDeathEvent(event: ThreadDeathEvent) {
    log.debug("--" + thread.name + " ends--")
  }

}
