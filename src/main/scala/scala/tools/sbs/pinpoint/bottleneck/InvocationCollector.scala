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

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor
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

  val currentGraph: InvocationGraph = collect
  val previousGraph: InvocationGraph = exploit(benchmark.pinpointPrevious, benchmark.context, collect)

  def collect: InvocationGraph = {
    val graph = new InvocationGraph
    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)
    val currentMethod = instrumentor.getMethod(
      methodName,
      className,
      config.classpathURLs ++ benchmark.classpathURLs)
    // inject name report for call expressions
    // launch benchmark
    graph
  }

  def isMatchOK = currentGraph matches previousGraph

}
