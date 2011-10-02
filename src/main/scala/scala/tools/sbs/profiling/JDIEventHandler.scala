/*
 * JDIEventHandler
 * 
 * Version
 * 
 * Created October 3rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.lang.InterruptedException

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.mutable.HashMap
import scala.tools.sbs.io.Log

import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.event.Event
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
import com.sun.jdi.ThreadReference
import com.sun.jdi.VMDisconnectedException
import com.sun.jdi.VirtualMachine

/** Handles JDI events.
 */
class JDIEventHandler(log: Log) {

  /** Packages to exclude from generating event.
   */
  private val excludes = List("java.*", "javax.*", "sun.*", "com.sun.*", "scala.*", "org.apache.*")

  /** Connected to target JVM.
   */
  private var connected = true

  /** Maps ThreadReference to ThreadTrace instances.
   */
  private val traceMap = new HashMap[ThreadReference, JDIThreadTrace]()

  /** Run the event handling thread. As long as we are connected, get event
   *  sets off the queue and dispatch the events within them.
   */
  def process(jvm: VirtualMachine): Profile = {
    val profile = new Profile
    setEventRequests(jvm)
    val queue = jvm.eventQueue
    jvm.resume
    while (connected) {
      try {
        val eventSet = queue.remove()
        val it = eventSet.eventIterator
        while (it hasNext) {
          handleEvent(it nextEvent, jvm, profile)
        }
        eventSet resume
      } catch {
        case _: InterruptedException => ()
        case exc: VMDisconnectedException => {
          handleDisconnectedException(jvm)
          log.info("Disconnected exception")
        }
      }
    }
    null
  }

  /** Create the desired event requests, and enable them so that we will get events.
   *
   *  @param excludes	Class patterns for which we don't want events
   */
  private def setEventRequests(jvm: VirtualMachine) {
    val mgr = jvm.eventRequestManager

    // want all exceptions
    val excReq = mgr.createExceptionRequest(null, true, true)
    // suspend so we can step
    excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    excReq.enable();

    val menr = mgr.createMethodEntryRequest
    excludes foreach (menr addClassExclusionFilter _)
    //        menr.addClassFilter("scala.tools.nsc.Main*")
    menr.addClassFilter("Sort*")
    menr.setSuspendPolicy(EventRequest.SUSPEND_NONE)
    menr enable

    val mexr = mgr.createMethodExitRequest
    excludes foreach (mexr addClassExclusionFilter _)
    //    mexr.addClassFilter("scala.tools.nsc.Main*")
    mexr.addClassFilter("Sort*")
    mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE)
    mexr enable

    val tdr = mgr.createThreadDeathRequest
    // Make sure we sync on thread death
    tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    tdr enable

    /*jvm.allThreads().asScala.toSeq foreach (thread => {
      val str = mgr.createStepRequest(thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)
      str.addClassFilter("scala.tools.nsc.util.ScalaClass*")
      str.setSuspendPolicy(EventRequest.SUSPEND_ALL)
      str.enable
    })*/

    val cpr = mgr.createClassPrepareRequest
    excludes foreach (cpr addClassExclusionFilter _)
    cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL)
    cpr enable
  }

  /** Dispatch incoming events
   */
  private def handleEvent(event: Event, jvm: VirtualMachine, profile: Profile) = {

    /** Returns the JDIThreadTrace instance for the specified thread, creating one if needed.
     */
    def threadTrace(thread: ThreadReference): JDIThreadTrace = {
      traceMap.get(thread) match {
        case None => {
          val ret = new JDIThreadTrace(log, profile, thread, jvm)
          traceMap.put(thread, ret)
          ret
        }
        case Some(trace) => trace
      }
    }

    /** A new class has been loaded. Set watchpoints on each of its fields.
     */
    def classPrepareEvent(cpe: ClassPrepareEvent) {
      val mgr = jvm.eventRequestManager
      cpe.referenceType.visibleFields.asScala.toSeq foreach (field => {
        val req = mgr createModificationWatchpointRequest field
        excludes foreach (req addClassExclusionFilter _)
        req.setSuspendPolicy(EventRequest.SUSPEND_NONE)
        req enable
      })

      log.debug("Prepared " + cpe.referenceType())
    }

    event match {
      case ee: ExceptionEvent => {
        threadTrace(ee.thread) exceptionEvent ee
      }
      case mwe: ModificationWatchpointEvent => {
        threadTrace(mwe.thread) fieldWatchEvent mwe
      }
      case mee: MethodEntryEvent => {
        threadTrace(mee.thread) methodEntryEvent mee
      }
      case mee: MethodExitEvent => {
        threadTrace(mee.thread) methodExitEvent mee
      }
      case se: StepEvent => {
        threadTrace(se.thread) stepEvent se
      }
      case tde: ThreadDeathEvent => {
        threadTrace(tde.thread) threadDeathEvent tde
      }
      case cpe: ClassPrepareEvent => {
        classPrepareEvent(cpe)
      }
      case vse: VMStartEvent => {
        log.debug("--JVM Started--")
      }
      case vde: VMDeathEvent => {
        log.debug("--Target JVM exited--")
      }
      case vde: VMDisconnectEvent => {
        vmDisconnectEvent
      }
      case _ => {
        throw new Error("Unexpected event type")
      }
    }
  }

  /** A VMDisconnectedException has happened while dealing with another event.
   *  We need to flush the event queue, dealing only with exit events (VMDeath,
   *  VMDisconnect) so that we terminate correctly.
   */
  private def handleDisconnectedException(jvm: VirtualMachine) {
    val queue = jvm.eventQueue
    while (connected) {
      try {
        val eventSet = queue.remove()
        val iter = eventSet.eventIterator
        while (iter hasNext) {
          iter.nextEvent match {
            case vde: VMDeathEvent => {
              log.debug("--Target JVM exited--")
            }
            case vde: VMDisconnectEvent => {
              vmDisconnectEvent()
            }
          }
        }
        eventSet.resume
      } catch {
        case _: InterruptedException => ()
      }
    }
  }

  private def vmDisconnectEvent() {
    connected = false
    log.info("--Target JVM disconnected--")
  }

}
