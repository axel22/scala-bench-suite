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
import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.Log
import scala.tools.sbs.Config

import com.sun.jdi.connect.Connector
import com.sun.jdi.connect.IllegalConnectorArgumentsException
import com.sun.jdi.connect.LaunchingConnector
import com.sun.jdi.connect.VMStartException
import com.sun.jdi.Bootstrap
import com.sun.jdi.VirtualMachine

/** Java Debug Interface based implement of {@link Profiler}.
 */
class JDIProfiler(log: Log, config: Config) extends Profiler {

  def profile(benchmark: Benchmark): Profile = {
    val command = JVMInvokerFactory(log, config) command benchmark
    val jvm = launchTarget(command mkString " ")
    try new JDIEventHandler(log, benchmark) process jvm
    catch {
      case exc: InterruptedException => null
    }
  }

  /** Launch target VM. Forward target's output and error.
   */
  private def launchTarget(mainArgs: String): VirtualMachine = {
    val connector =
      Bootstrap.virtualMachineManager.allConnectors.asScala.toSeq find (
        _.name equals "com.sun.jdi.CommandLineLaunch") match {
          case Some(cnt) => cnt.asInstanceOf[LaunchingConnector]
          case None => throw new Error("No launching connector")
        }
    val arguments = connectorArguments(connector, mainArgs)
    try connector launch arguments
    catch {
      case exc: IOException => throw new Error("Unable to launch target VM: " + exc)
      case exc: IllegalConnectorArgumentsException => throw new Error("Internal error: " + exc)
      case exc: VMStartException => throw new Error("Target VM failed to initialize: " + exc.getMessage)
    }
  }

  /** Return the launching connector's arguments.
   */
  private def connectorArguments(connector: LaunchingConnector, mainArgs: String): JMap[String, Connector.Argument] = {
    val arguments = connector.defaultArguments

    val mainArg = arguments get "main"
    if (mainArg == null) {
      throw new Error("Bad launching connector")
    }
    mainArg setValue mainArgs

    // We need a VM that supports watchpoints
    val optionArg = arguments get "options"
    if (optionArg == null) {
      throw new Error("Bad launching connector")
    }
    optionArg setValue "-classic"

    arguments
  }

}
