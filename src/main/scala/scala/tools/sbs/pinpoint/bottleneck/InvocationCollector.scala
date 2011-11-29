/*
 * InvocationCollector
 * 
 * Version
 * 
 * Created on November 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package bottleneck

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.common.RunOnlyHarness
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.JavaUtility
import scala.tools.sbs.pinpoint.strategy.InstrumentationRunner
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter

class InvocationCollector(val config: Config,
                          val log: Log,
                          benchmark: PinpointBenchmark,
                          className: String,
                          methodName: String,
                          val instrumentedOut: Directory,
                          val backupPlace: Directory)
  extends Configured
  with InstrumentationRunner
  with PreviousVersionExploiter {

  def graph: InvocationGraph = currentGraph

  def isMatchOK = currentGraph matches previousGraph

  val currentGraph: InvocationGraph = collect(config.classpathURLs ++ benchmark.classpathURLs)

  val previousGraph: InvocationGraph = exploit(
    benchmark.pinpointPrevious,
    benchmark.context,
    config.classpathURLs ++ benchmark.classpathURLs,
    collect)

  private def collect(classpathURLs: List[URL]): InvocationGraph = {

    val graph = new InvocationGraph
    var recursionDepth = 0

    def addToGraph(line: String) = try scala.xml.XML loadString line match {
      case <call><class>{ clazz }</class><method>{ method }</method><signature>{ signature }</signature></call> =>
        if (recursionDepth == 1) graph.add(clazz.text, method.text, signature.text)
      case <entry/> => recursionDepth += 1
      case <exit/>  => recursionDepth -= 1
      case _        => throw new Exception
    }
    catch { case _ => log(line) }

    def callNotifying(clazz: String, method: String, signature: String) =
      JavaUtility.javaSysout(JavaUtility.doubleQuote(
        "<call>" +
          "<class>" + clazz + "</class>" +
          "<method>" + method + "</method>" +
          "<signature>" + signature + "	</signature>" +
          "</call>"))

    val invoker = JVMInvokerFactory(log, config)

    instrumentAndRun(
      benchmark,
      className,
      methodName,
      (method, instrumentor) => {
        instrumentor.notifyCallExpression(method, callNotifying)
        instrumentor.sandwich(
          method,
          JavaUtility.javaSysout(JavaUtility.doubleQuote("<entry/>")),
          JavaUtility.javaSysout(JavaUtility.doubleQuote("<exit/>")))
      },
      classpathURLs,
      cpURLs => invoker.invoke(
        invoker.command(RunOnlyHarness, benchmark, cpURLs),
        addToGraph,
        log.error,
        benchmark.timeout))

    graph
  }

}
