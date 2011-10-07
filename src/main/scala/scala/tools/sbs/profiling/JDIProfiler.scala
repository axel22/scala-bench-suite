/*
 * JDIProfiler
 * 
 * Version
 * 
 * Created on October 3rd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs.profiling

import java.io.IOException
import java.util.{ Map => JMap }

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.Config
import scala.tools.sbs.Profiling

import com.sun.jdi.connect.Connector
import com.sun.jdi.connect.IllegalConnectorArgumentsException
import com.sun.jdi.connect.LaunchingConnector
import com.sun.jdi.connect.VMStartException
import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachine

/** Java Debug Interface based implement of {@link Profiler}.
 */
class JDIProfiler(config: Config) extends Profiler {

  def profile(benchmark: Benchmark): ProfilingResult = {
    log = benchmark createLog Profiling
    //    val command = JVMInvokerFactory(log, config) command benchmark
    val command = List(
      "-cp",
      "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-library.jar;" +
        "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-compiler.jar",
      "-Dscala.home=D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final",
      "scala.tools.nsc.MainGenericRunner",
      "-cp",
      "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-library.jar;" +
        "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-compiler.jar;" +
        config.bin.path,
      benchmark.name) ++ benchmark.arguments

    log.debug("Profile command: " + (command mkString " "))

    val jvm = launchTarget(command mkString " ")
    try {
      val profile = new JDIEventHandler(log, benchmark) process jvm
      ProfilingSuccess(benchmark, profile)
    }
    catch {
      // TODO
      case exc: Exception => {
        log.info(exc.toString)
        log.info(exc.getStackTraceString)
        jvm.exit(1)
        ProfilingException(benchmark, exc)
      }
      case exc: IOException                        => throw new Error("Unable to launch target VM: " + exc)
      case exc: IllegalConnectorArgumentsException => throw new Error("Internal error: " + exc)
      case exc: VMStartException                   => throw new Error("Target VM failed to initialize: " + exc.getMessage)
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
