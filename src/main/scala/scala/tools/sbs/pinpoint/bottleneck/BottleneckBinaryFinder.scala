/*
 * BottleneckBinaryFinder
 * 
 * Version
 * 
 * Created on October 28th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package bottleneck

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.regression.ANOVARegressionFailure
import scala.tools.sbs.performance.regression.CIRegressionFailure
import scala.tools.sbs.performance.regression.CIRegressionSuccess
import scala.tools.sbs.performance.regression.RegressionFailure
import scala.tools.sbs.pinpoint.instrumentation.JavaUtility
import scala.tools.sbs.pinpoint.strategy.InstrumentationRunner
import scala.tools.sbs.pinpoint.strategy.PinpointMeasurerFactory
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter
import scala.tools.sbs.pinpoint.strategy.TwinningDetector
import scala.tools.sbs.pinpoint.BottleneckUndetectableException

/** Uses a binary-search-like algorithm to find the bottleneck in a
 *  method call list of a method.
 */
class BottleneckBinaryFinder(val config: Config,
                             val log: Log,
                             benchmark: PinpointBenchmark,
                             declaringClass: String,
                             bottleneckMethod: String,
                             graph: InvocationGraph,
                             val instrumentedOut: Directory,
                             val backupPlace: Directory)
  extends BottleneckFinder
  with Configured
  with TwinningDetector
  with InstrumentationRunner
  with PreviousVersionExploiter {

  def find(): BottleneckFound = binaryFind(graph)

  private def binaryFind(graph: InvocationGraph): BottleneckFound = {
    def narrow(regressionFailure: RegressionFailure): BottleneckFound = {
      /** Creates only incase necessary.
       */
      def currentBottleneck = regressionFailure match {
        case CIRegressionFailure(_, current, previous, ci) => {
          Bottleneck(
            benchmark,
            graph,
            current,
            previous,
            ci)
        }
        case _: ANOVARegressionFailure => throw new ANOVAUnsupportedException
        case _                         => throw new AlgorithmFlowException(this.getClass)
      }
      if (graph.length > 1) {
        val (firstHalf, secondHalf) = graph.split
        try {
          lazy val secondBottleNeck = binaryFind(secondHalf)
          val firstBottleNeck =
            try { binaryFind(firstHalf) }
            catch {
              case _: BottleneckUndetectableException =>
                secondBottleNeck match {
                  case _: NoBottleneck => currentBottleneck
                  case _               => secondBottleNeck
                }
            }
          firstBottleNeck match {
            case noBottleneck: NoBottleneck => {
              secondBottleNeck match {
                case _: NoBottleneck => currentBottleneck
                case _               => secondBottleNeck
              }
            }
            case _ => { firstBottleNeck }
          }
        }
        catch { case _: BottleneckUndetectableException => currentBottleneck }
      }
      else { currentBottleneck }
    }

    if (graph.length == 1) {
      log.info("  Checking whether the " + graph.startOrdinum + " time invocation " +
        "of method call " + graph.first.prototype + "is a bottleneck")
    }
    else {
      log.info("  Finding bottleneck between " +
        "the " + graph.startOrdinum + " time invocation of method call " + graph.first.prototype +
        " and the " + graph.endOrdinum + " time invocation of method call " + graph.last.prototype)
    }
    log.info("")

    twinningDetect(
      benchmark,
      measureCurrent(graph),
      measurePrevious(graph),
      regressOK => regressOK match {
        case ciOK: CIRegressionSuccess =>
          NoBottleneck(benchmark, regressOK.confidenceLevel, ciOK.current, ciOK.previous, ciOK.CI)
        case _ =>
          throw new ANOVAUnsupportedException
      },
      narrow,
      _ => throw new BottleneckUndetectableException(declaringClass, bottleneckMethod, graph))
  }

  private def measureCurrent(graph: InvocationGraph) =
    measureCommon(graph, config.classpathURLs ++ benchmark.classpathURLs)

  private def measurePrevious(graph: InvocationGraph) = exploit(
    benchmark.pinpointPrevious,
    benchmark.context,
    config.classpathURLs ++ benchmark.classpathURLs,
    measureCommon(graph, _))

  private def measureCommon(graph: InvocationGraph, classpathURLs: List[URL]) =
    instrumentAndRun(
      benchmark,
      declaringClass,
      bottleneckMethod,
      (method, instrumentor) => {
        instrumentor.insertBeforeCall(method, graph.first.prototype, JavaUtility.callPinpointHarnessStart)
        instrumentor.insertAfterCall(method, graph.first.prototype, JavaUtility.callPinpointHarnessEnd)
      },
      classpathURLs,
      PinpointMeasurerFactory(config, log).measure(benchmark, _))

}
