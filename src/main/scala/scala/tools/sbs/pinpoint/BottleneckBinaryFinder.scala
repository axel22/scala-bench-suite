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

import java.net.URL
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.pinpoint.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.regression.ANOVARegressionFailure
import scala.tools.sbs.regression.CIRegressionFailure
import scala.tools.sbs.regression.RegressionFailure
import scala.tools.sbs.regression.CIRegressionSuccess

/** Uses a binary-search-like algorithm to find the bottleneck in a
 *  method call list of a method.
 */
class BottleneckBinaryFinder(protected val log: Log,
                             protected val config: Config,
                             benchmark: PinpointBenchmark,
                             instrumentor: CodeInstrumentor,
                             instrumentedURL: URL)
  extends BottleneckFinder
  with TwinningDetector[BottleneckFound] {

  def find(): BottleneckFound = {
    val currentMethod = instrumentor.getMethod(
      benchmark.pinpointMethod,
      benchmark.pinpointClass,
      config.classpathURLs ++ benchmark.classpathURLs)

    val previousMethod = instrumentor.getMethod(
      benchmark.pinpointMethod,
      benchmark.pinpointClass,
      benchmark.pinpointPrevious.toURL :: config.classpathURLs ++ benchmark.classpathURLs)

    val currentCallingList = instrumentor callListOf currentMethod
    val previousCallingList = instrumentor callListOf previousMethod
    if ((currentCallingList map (call => (call.getClassName, call.getMethodName, call.getSignature))) !=
      (previousCallingList map (call => (call.getClassName, call.getMethodName, call.getSignature)))) {
      throw new MismatchExpressionList(benchmark, currentCallingList, previousCallingList)
    }

    var callIndexList = List[Int]()
    currentCallingList foreach (_ => callIndexList :+= callIndexList.length)

    binaryFind(callIndexList, currentCallingList)
  }

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
      (_, _) => throw new BottleneckUndetectableException(benchmark, callList))
  }

  private def measureCurrent(callIndexList: List[Int]): MeasurementResult =
    classpathBasedMeasure(callIndexList, config.classpathURLs ++ benchmark.classpathURLs)

  private def measurePrevious(callIndexList: List[Int]): MeasurementResult =
    classpathBasedMeasure(
      callIndexList, benchmark.pinpointPrevious.toURL :: config.classpathURLs ++ benchmark.classpathURLs)

  private def classpathBasedMeasure(callIndexList: List[Int], classpathURLs: List[URL]): MeasurementResult = {
    val (clazz, method) = instrumentor.getClassAndMethod(
      benchmark.pinpointClass, benchmark.pinpointMethod, classpathURLs)
    instrumentor.sandwichCallList(
      method,
      callIndexList.head, PinpointHarness.javaInstructionCallStart,
      callIndexList.last, PinpointHarness.javaInstructionCallEnd)
    instrumentor.writeFile(clazz, instrumentedURL)
    PinpointMeasurerFactory(config, log).measure(benchmark, instrumentedURL :: classpathURLs)
  }

}
