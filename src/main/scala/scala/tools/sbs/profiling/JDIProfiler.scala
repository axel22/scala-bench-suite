/*
 * JDIProfiler
 * 
 * Version
 * 
 * Created on October 3rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.io.IOException
import java.util.{ Map => JMap }

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.Log

import com.sun.jdi.connect.Connector
import com.sun.jdi.connect.LaunchingConnector
import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachine

/** Java Debug Interface based implement of {@link Profiler}.
 */
class JDIProfiler(val config: Config, val log: Log) extends Profiler {

  protected def profile(benchmark: ProfilingBenchmark): ProfilingResult = {
    val javaArgument =
      JVMInvokerFactory(log, config).asJavaArgument(benchmark, config.classpathURLs ++ benchmark.classpathURLs)

    log.debug("Profile command: " + (javaArgument mkString " "))

    val jvm = launchTarget(javaArgument mkString " ")

    def reportException(exc: Exception): ProfilingException = {
      log.error(exc.toString)
      log.error(exc.getStackTraceString)
      jvm.exit(1)
      ProfilingException(benchmark, exc)
    }

    try {
      val profile =
        if ((benchmark.profileMethod == "") &&
          (benchmark.profileField == "") &&
          !config.shouldGC &&
          !config.shouldBoxing) {
          new JDIEventHandler(log, config, benchmark) process jvm
        }
        else {
          new Profile
        }
      if (config.shouldGC) {

        new MemoryProfiler(log, config).profile(benchmark, profile)
      }
      else {
        ProfilingSuccess(benchmark, profile)
      }
    }
    catch {
      case exc: IOException => reportException(new IOException("Unable to launch target VM: " + exc))
      case exc: Exception   => reportException(exc)
    }
  }

  /** Launch target VM. Forward target's output and error.
   */
  private def launchTarget(mainArgs: String): VirtualMachine = {
    val connector =
      Bootstrap.virtualMachineManager.allConnectors.asScala.toSeq find (
        _.name equals "com.sun.jdi.CommandLineLaunch") match {
          case Some(cnt) => cnt.asInstanceOf[LaunchingConnector]
          case None      => throw new Exception("No launching connector")
        }
    val arguments = connectorArguments(connector, mainArgs)
    connector launch arguments
  }

  /** Return the launching connector's arguments.
   */
  private def connectorArguments(connector: LaunchingConnector, mainArgs: String): JMap[String, Connector.Argument] = {
    val arguments = connector.defaultArguments

    val mainArg = arguments get "main"
    if (mainArg == null) {
      throw new Exception("Bad launching connector")
    }
    mainArg setValue mainArgs

    // We need a VM that supports watchpoints
    val optionArg = arguments get "options"
    if (optionArg == null) {
      throw new Exception("Bad launching connector")
    }
    optionArg setValue "-classic"

    arguments
  }

}
