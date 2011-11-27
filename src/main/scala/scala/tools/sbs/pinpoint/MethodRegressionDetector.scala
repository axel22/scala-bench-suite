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

import java.net.URL

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.regression.CIRegressionFailure
import scala.tools.sbs.performance.regression.CIRegressionSuccess
import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.pinpoint.strategy.InstrumentationRunner
import scala.tools.sbs.pinpoint.strategy.PinpointHarness
import scala.tools.sbs.pinpoint.strategy.PinpointMeasurerFactory
import scala.tools.sbs.pinpoint.strategy.PreviousVersionExploiter
import scala.tools.sbs.pinpoint.strategy.TwinningDetector

class MethodRegressionDetector(val config: Config,
                               val log: Log,
                               benchmark: PinpointBenchmark,
                               val instrumentedOut: Directory,
                               val backupPlace: Directory)
  extends ScrutinyRegressionDetector
  with TwinningDetector
  with Configured
  with InstrumentationRunner
  with PreviousVersionExploiter {

  def detect(stupidDummyNotTobeUsedBenchmark: PinpointBenchmark): ScrutinyRegressionResult = {
    if (benchmark.pinpointClass == "" || benchmark.pinpointMethod == "") {
      throw new NoPinpointingMethodException(benchmark)
    }

    log.info("Detecting performance regression of method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod)
    log.info("")

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

  private lazy val measureCurrent = measureCommon(config.classpathURLs ++ benchmark.classpathURLs)

  private lazy val measurePrevious = exploit(
    benchmark.pinpointPrevious,
    benchmark.context,
    measureCommon(config.classpathURLs ++ benchmark.classpathURLs :+ benchmark.pinpointPrevious.toURL))

  private def measureCommon(classpathURLs: List[URL]) = instrumentAndRun(
    benchmark,
    benchmark.pinpointClass,
    benchmark.pinpointMethod,
    (method, instrumentor) => instrumentor.sandwich(
      method,
      PinpointHarness.javaInstructionCallStart,
      PinpointHarness.javaInstructionCallEnd),
    PinpointMeasurerFactory(config, log).measure(benchmark, instrumentedOut.toURL :: classpathURLs),
    classpathURLs)

}
