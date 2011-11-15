/*
 * MethodRegressionDetector
 * 
 * Version
 * 
 * Created on November 1st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.performance.regression.CIRegressionFailure
import scala.tools.sbs.performance.regression.CIRegressionSuccess
import scala.tools.sbs.performance.MeasurementFailure
import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.pinpoint.strategy.InstrumentationMeasurer
import scala.tools.sbs.pinpoint.strategy.PinpointHarness
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter
import scala.tools.sbs.pinpoint.strategy.TwinningDetector

class MethodRegressionDetector(protected val config: Config,
                               protected val log: Log,
                               benchmark: PinpointBenchmark,
                               instrumentedOut: Directory,
                               backup: Directory)
  extends InstrumentationMeasurer(
    config,
    log,
    benchmark,
    instrumentedOut,
    backup)
  with TwinningDetector
  with PreviousVersionExploiter
  with ScrutinyRegressionDetector {

  def detect(stupidDummyNotTobeUsedBenchmark: PinpointBenchmark): ScrutinyRegressionResult = {
    if (benchmark.pinpointClass == "" || benchmark.pinpointMethod == "") {
      throw new NoPinpointingMethodException(benchmark)
    }

    UI.info("Detecting performance regression of method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod)
    log.info("Detecting performance regression of method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod)
    UI.info("")

    twinningDetect(
      benchmark,
      measureCurrent,
      measurePrevious,
      regressOK => regressOK match {
        case ci: CIRegressionSuccess => ScrutinyCIRegressionSuccess(ci)
        case _                       => throw new ANOVAUnsupportedException
      },
      regressFailed => regressFailed match {
        case ci: CIRegressionFailure => (measureCurrent, measurePrevious) match {
          case (current: MeasurementSuccess, previous: MeasurementSuccess) =>
            ScrutinyCIRegressionFailure(ci)
          case _ => throw new AlgorithmFlowException(this.getClass)
        }
        case _ => throw new ANOVAUnsupportedException
      },
      failure => ScrutinyImmeasurableFailure(benchmark, failure))
  }

  private lazy val measureCurrent = instrumentAndMeasure(
    benchmark.pinpointClass,
    benchmark.pinpointMethod,
    (method, instrumentor) => instrumentor.sandwich(
      method,
      PinpointHarness.javaInstructionCallStart,
      PinpointHarness.javaInstructionCallEnd),
    config.classpathURLs ++ benchmark.classpathURLs)

  private lazy val measurePrevious = exploit(
    benchmark.pinpointPrevious,
    benchmark.context,
    backup,
    instrumentAndMeasure(
      benchmark.pinpointClass,
      benchmark.pinpointMethod,
      (method, instrumentor) => instrumentor.sandwich(
        method,
        PinpointHarness.javaInstructionCallStart,
        PinpointHarness.javaInstructionCallEnd),
      config.classpathURLs ++ benchmark.classpathURLs :+ benchmark.pinpointPrevious.toURL))

}
