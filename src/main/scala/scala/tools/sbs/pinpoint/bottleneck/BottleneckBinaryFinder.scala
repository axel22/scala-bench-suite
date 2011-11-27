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
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.pinpoint.strategy.InstrumentationRunner
import scala.tools.sbs.pinpoint.strategy.PinpointHarness
import scala.tools.sbs.pinpoint.strategy.PinpointMeasurerFactory
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter
import scala.tools.sbs.pinpoint.strategy.TwinningDetector

/** Uses a binary-search-like algorithm to find the bottleneck in a
 *  method call list of a method.
 */
class BottleneckBinaryFinder(val config: Config,
                             val log: Log,
                             benchmark: PinpointBenchmark,
                             declaringClass: String,
                             bottleneckMethod: String,
                             callIndexList: List[Int],
                             callList: List[MethodCallExpression],
                             val instrumentedOut: Directory,
                             val backupPlace: Directory)
  extends BottleneckFinder
  with Configured
  with TwinningDetector
  with InstrumentationRunner
  with PreviousVersionExploiter {

  def find(): BottleneckFound = binaryFind(callIndexList, callList)

  private def binaryFind(callIndexList: List[Int], callList: List[MethodCallExpression]): BottleneckFound = {
    def narrow(regressionFailure: RegressionFailure): BottleneckFound = {
      /** Creates only incase necessary.
       */
      def currentBottleneck = regressionFailure match {
        case CIRegressionFailure(_, current, previous, ci) => {
          Bottleneck(
            benchmark,
            callList.slice(callIndexList.head, callIndexList.last + 1),
            current,
            previous,
            ci)
        }
        case _: ANOVARegressionFailure => throw new ANOVAUnsupportedException
        case _                         => throw new AlgorithmFlowException(this.getClass)
      }
      if (callIndexList.length > 1) {
        val (firstHalf, secondHalf) = callIndexList partition (callIndexList.indexOf(_) < callIndexList.length / 2)
        try {
          lazy val secondBottleNeck = binaryFind(secondHalf, callList)
          val firstBottleNeck =
            try { binaryFind(firstHalf, callList) }
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

    val start = callList(callIndexList.head)
    val end = callList(callIndexList.last)
    def position(call: MethodCallExpression) =
      call.getClassName + "." + call.getMethodName + call.getSignature + " at line " + call.getLineNumber

    if (callIndexList.length == 1) {
      log.info("  Checking whether method call " + position(start) + " is a bottleneck")
    }
    else {
      log.info("  Finding bottleneck between " + "method call " + position(start) + " and " + position(end))
    }
    log.info("")

    twinningDetect(
      benchmark,
      measureCurrent(callIndexList),
      measurePrevious(callIndexList),
      regressOK => regressOK match {
        case ciOK: CIRegressionSuccess =>
          NoBottleneck(benchmark, regressOK.confidenceLevel, ciOK.current, ciOK.previous, ciOK.CI)
        case _ =>
          throw new ANOVAUnsupportedException
      },
      narrow,
      _ => throw new BottleneckUndetectableException(benchmark, callList))
  }

  private def measureCurrent(callIndexList: List[Int]) =
    measureCommon(callIndexList: List[Int], config.classpathURLs ++ benchmark.classpathURLs)

  private def measurePrevious(callIndexList: List[Int]) = exploit(
    benchmark.pinpointPrevious,
    benchmark.context,
    measureCommon(callIndexList, config.classpathURLs ++ benchmark.classpathURLs :+ benchmark.pinpointPrevious.toURL))

  private def measureCommon(callIndexList: List[Int], classpathURLs: List[URL]) =
    instrumentAndRun(
      benchmark,
      declaringClass,
      bottleneckMethod,
      (method, instrumentor) => instrumentor.sandwichCallList(
        method,
        callIndexList.head, PinpointHarness.javaInstructionCallStart,
        callIndexList.last, PinpointHarness.javaInstructionCallEnd),
      PinpointMeasurerFactory(config, log).measure(benchmark, instrumentedOut.toURL :: classpathURLs),
      classpathURLs)

}
