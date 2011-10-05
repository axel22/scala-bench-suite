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
import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.io.Log

import com.sun.jdi.event.AccessWatchpointEvent
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
import com.sun.jdi.request.DuplicateRequestException
import com.sun.jdi.request.EventRequest
import com.sun.jdi.request.StepRequest
import com.sun.jdi.ThreadReference
import com.sun.jdi.VMDisconnectedException
import com.sun.jdi.VirtualMachine

/** Handles JDI events.
 */
class JDIEventHandler(log: Log, benchmark: Benchmark) {

  /** Packages to exclude from generating class prepared event.
   */
  private val classExcludes = List("java.*", "javax.*", "sun.*", "com.sun.*", "scala.*", "org.apache.*")

  /** Packages to exclude from generation method event.
   */
  private val methodExcludes = List("java.*", "javax.*", "sun.*", "com.sun.*", "org.apache.*")
  
  /** Connected to target JVM.
   */
  private var connected = true

  /** Maps ThreadReference to ThreadTrace instances.
   */
  private val traceMap = new HashMap[ThreadReference, JDIThreadTrace]()

  /** Whether the main benchmark class has been loaded.
   */
  private var mainLoaded = false

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
    profile
  }

  /** Create the desired event requests, and enable them so that we will get events.
   */
  private def setEventRequests(jvm: VirtualMachine) {
    val mgr = jvm.eventRequestManager

    // want all exceptions
    val excReq = mgr.createExceptionRequest(null, true, true)
    // suspend so we can step
    excReq setSuspendPolicy EventRequest.SUSPEND_ALL
    excReq enable

    val tdr = mgr.createThreadDeathRequest
    // Make sure we sync on thread death
    tdr setSuspendPolicy EventRequest.SUSPEND_ALL
    tdr enable

    val cpr = mgr.createClassPrepareRequest
    classExcludes foreach (cpr addClassExclusionFilter _)
    cpr setSuspendPolicy EventRequest.SUSPEND_ALL
    cpr enable
  }

  /** Dispatch incoming events
   */
  private def handleEvent(event: Event, jvm: VirtualMachine, profile: Profile) {

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

    /** A new class has been loaded.
     *  <ul>
     *  <li>Test whether it is the benchmark main class
     *  <li>If so, set event requests for fields, methods and steps
     *  <li>Otherwise, add to profile
     *  <ul>
     */
    def classPrepareEvent(event: ClassPrepareEvent) {
      log.debug("Prepared " + event.referenceType)

      if ((event.referenceType.name equals benchmark.name) || (mainLoaded)) {

        profile loadClass event.referenceType.name

        val mgr = jvm.eventRequestManager

        // Add watchpoint requests
        event.referenceType.visibleFields.asScala.toSeq foreach (field => {
          val mwrReq = mgr createModificationWatchpointRequest field
          classExcludes foreach (mwrReq addClassExclusionFilter _)
          mwrReq setSuspendPolicy EventRequest.SUSPEND_NONE
          mwrReq enable

          val awrReq = mgr createAccessWatchpointRequest field
          classExcludes foreach (awrReq addClassExclusionFilter _)
          awrReq setSuspendPolicy EventRequest.SUSPEND_NONE
          awrReq enable
        })

        // Add step request
        try {
          val str = mgr.createStepRequest(event.thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)
          str setSuspendPolicy EventRequest.SUSPEND_ALL
          str enable
        } catch {
          case _: DuplicateRequestException => ()
        }

        if (!mainLoaded) {
          mainLoaded = true
          // Add method entry request
          val menr = mgr.createMethodEntryRequest
          methodExcludes foreach (menr addClassExclusionFilter _)
          menr setSuspendPolicy EventRequest.SUSPEND_NONE
          menr enable

          // Add method exit request
          val mexr = mgr.createMethodExitRequest
          methodExcludes foreach (mexr addClassExclusionFilter _)
          mexr setSuspendPolicy EventRequest.SUSPEND_NONE
          mexr enable

        }
      }
    }

    event match {
      case ee: ExceptionEvent => {
        threadTrace(ee.thread) exceptionEvent ee
      }
      case awe: AccessWatchpointEvent => {
        threadTrace(awe.thread) fieldAccessEvent awe
      }
      case mwe: ModificationWatchpointEvent => {
        threadTrace(mwe.thread) fieldModifyEvent mwe
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
