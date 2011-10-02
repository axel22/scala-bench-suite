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

import java.lang.StringBuffer
import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi.event.MethodEntryEvent
import com.sun.jdi.event.MethodExitEvent
import com.sun.jdi.event.ModificationWatchpointEvent
import com.sun.jdi.event.StepEvent
import com.sun.jdi.event.ThreadDeathEvent
import com.sun.jdi.event.VMDeathEvent
import com.sun.jdi.event.VMDisconnectEvent
import com.sun.jdi.event.VMStartEvent
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.StepRequest
import com.sun.jdi.IncompatibleThreadStateException
import com.sun.jdi.ThreadReference
import com.sun.jdi.VirtualMachine
import scala.tools.sbs.io.Log

/** This class keeps context on events in one thread.
 */
class JDIThreadTrace(log: Log, profile: Profile, thread: ThreadReference, jvm: VirtualMachine) {
  
  def methodEntryEvent(event: MethodEntryEvent) {
//    if (event.method().name() contains "run") {
      println("**** " + event.method.name + "  --  " + event.method.declaringType.name)
//      event.method.arguments.asScala.toSeq foreach println
      /* event.method().arguments().asScala.toSeq foreach (v => {
      event.thread().frames().asScala.foreach(f => {
        try f.getValue(v)
        catch {
          case _ => ()
        }
      })
    })*/

//    }
//    if (event.method().name() contains "ontex") {
//      println("**** " + event.method.name + "  --  " + event.method.declaringType.name)
//      event.method.arguments.asScala.toSeq foreach println
//    }
    indent append "| "
  }

  def methodExitEvent(event: MethodExitEvent) {
    indent.setLength(indent.length - 2)
  }

  def fieldWatchEvent(event: ModificationWatchpointEvent) {
    println("    " + event.field.name + " = " + event.valueToBe)
  }

  def exceptionEvent(event: ExceptionEvent) {
    println("Exception: " + event.exception + " catch: " + event.catchLocation)

    // Step to the catch
    val mgr = jvm.eventRequestManager
    val req = mgr.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)
    req.addCountFilter(1) // next step only
    req.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    req.enable()
  }

  // Step to exception catch
  def stepEvent(event: StepEvent) {
    // Adjust call depth
    var cnt = 0
    indent = new StringBuffer(baseIndent)
    try {
      cnt = thread.frameCount
    } catch {
      case a: IncompatibleThreadStateException => ()
    }
    cnt -= 1
    while (cnt > 0) {
      indent append "| "
      cnt -= 1
    }

    println(event.location().method())

    val mgr = jvm.eventRequestManager
    mgr deleteEventRequest event.request
  }

  def threadDeathEvent(event: ThreadDeathEvent) {
    indent = new StringBuffer(baseIndent)
    println("====== " + thread.name() + " end ======")
  }

}
