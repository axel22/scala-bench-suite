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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.UI
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
    val javaArgument = JVMInvokerFactory(log, config) asJavaArgument benchmark
    /*List(
      "-cp",
      "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-library.jar;" +
        "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-compiler.jar",
      "-Dscala.home=D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final",
      "scala.tools.nsc.MainGenericRunner",
      "-cp",
      "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-library.jar;" +
        "D:\\University\\5thYear\\Internship\\Working\\scala-2.9.1.final\\lib\\scala-compiler.jar;" +
        config.bin.path,
      benchmark.name) ++ benchmark.arguments*/

    UI.debug("Profile command: " + (javaArgument mkString " "))
    log.debug("Profile command: " + (javaArgument mkString " "))

    val jvm = launchTarget(javaArgument mkString " ")

    def reportException(exc: Exception): ProfilingException = {
      log.info(exc.toString)
      log.info(exc.getStackTraceString)
      jvm.exit(1)
      ProfilingException(benchmark, exc)
    }

    try {
      val profile = new JDIEventHandler(log, config, benchmark) process jvm
      ProfilingSuccess(benchmark, profile)
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
