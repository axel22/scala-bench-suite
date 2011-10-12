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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.Config

import com.sun.jdi.event.AccessWatchpointEvent
import com.sun.jdi.event.ClassPrepareEvent
import com.sun.jdi.event.ClassUnloadEvent
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
class JDIEventHandler(log: Log, config: Config, benchmark: Benchmark) {

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
      }
      catch {
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
    //    val excReq = mgr.createExceptionRequest(null, true, true)
    //    excReq setSuspendPolicy EventRequest.SUSPEND_ALL
    //    excReq enable

    val tdr = mgr.createThreadDeathRequest
    // Make sure we sync on thread death
    tdr setSuspendPolicy EventRequest.SUSPEND_ALL
    tdr enable

    println(benchmark.profiledClasses)
    println(benchmark.excludeClasses)
    benchmark.profiledClasses foreach (pattern => {
      val cpr = mgr.createClassPrepareRequest
      benchmark.excludeClasses foreach (cpr addClassExclusionFilter _)
      cpr addClassFilter pattern
      cpr setSuspendPolicy EventRequest.SUSPEND_ALL
      cpr enable
    })

    if (config.shouldBoxing) List(
      "scala.runtime.BoxesRunTime",
      "scala.Predef",
      "scala.Int",
      "scala.Short",
      "scala.Long",
      "scala.Double",
      "scala.Float",
      "scala.Boolean",
      "scala.Byte",
      "scala.Char") foreach (boxingClass => {
        val boxingMethodRequest = mgr.createMethodEntryRequest
        boxingMethodRequest addClassFilter boxingClass
        boxingMethodRequest setSuspendPolicy EventRequest.SUSPEND_NONE
        boxingMethodRequest enable
      })
  }

  /** Dispatch incoming events
   */
  private def handleEvent(event: Event, jvm: VirtualMachine, profile: Profile) {

    /** Returns the JDIThreadTrace instance for the specified thread, creating one if needed.
     */
    def threadTrace(thread: ThreadReference): JDIThreadTrace = {
      traceMap.get(thread) match {
        case None => {
          val ret = new JDIThreadTrace(log, profile, benchmark, thread, jvm)
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
      log.verbose("Prepared " + event.referenceType)

      profile loadClass event.referenceType.name

      val mgr = jvm.eventRequestManager

      // Add watchpoint requests
      event.referenceType.visibleFields.asScala.toSeq find (_.name == benchmark.profiledField) match {
        case Some(field) => {
          val mwrReq = mgr createModificationWatchpointRequest field
          mwrReq setSuspendPolicy EventRequest.SUSPEND_NONE
          mwrReq enable

          val awrReq = mgr createAccessWatchpointRequest field
          awrReq setSuspendPolicy EventRequest.SUSPEND_NONE
          awrReq enable
        }
        case None => ()
      }

      // Add step request
      if (config.shouldStep) {
        try {
          val str = mgr.createStepRequest(event.thread, StepRequest.STEP_MIN, StepRequest.STEP_INTO)
          benchmark.excludeClasses foreach (str addClassExclusionFilter _)
          str setSuspendPolicy EventRequest.SUSPEND_NONE
          str enable
        }
        catch { case _: DuplicateRequestException => () }
      }

      // Add method entry request
      val menr = mgr.createMethodEntryRequest
      menr addClassFilter event.referenceType.name
      menr setSuspendPolicy EventRequest.SUSPEND_NONE
      menr enable
      
      // Add method exit request
      val mexr = mgr.createMethodExitRequest
      mexr addClassFilter event.referenceType.name
      mexr setSuspendPolicy EventRequest.SUSPEND_NONE
      mexr enable
    }

    def classUnloadEvent(event: ClassUnloadEvent) {
      log.verbose("Unloaded " + event.className)
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
      case cue: ClassUnloadEvent => {
        classUnloadEvent(cue)
      }
      case vse: VMStartEvent => {
        log.info("--JVM Started--")
      }
      case vde: VMDeathEvent => {
        log.info("--Target JVM exited--")
      }
      case vde: VMDisconnectEvent => {
        vmDisconnectEvent
      }
      case _ => {
        // TODO
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
              log.info("--Target JVM exited--")
            }
            case vde: VMDisconnectEvent => {
              vmDisconnectEvent()
            }
          }
        }
        eventSet.resume
      }
      catch { case _: InterruptedException => () }
    }
  }

  private def vmDisconnectEvent() {
    connected = false
    log.info("--Target JVM disconnected--")
  }

}
