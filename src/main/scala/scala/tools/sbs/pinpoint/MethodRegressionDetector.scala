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

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.regression.CIRegressionFailure
import scala.tools.sbs.performance.regression.CIRegressionSuccess
import scala.tools.sbs.performance.MeasurementFailure
import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor
import scala.tools.sbs.pinpoint.strategy.InstrumentationMeasurer
import scala.tools.sbs.pinpoint.strategy.PinpointHarness
import scala.tools.sbs.pinpoint.strategy.TwinningDetector

class MethodRegressionDetector(protected val config: Config,
                               protected val log: Log,
                               benchmark: PinpointBenchmark,
                               instrumentor: CodeInstrumentor,
                               instrumented: Directory,
                               backup: Directory)
  extends InstrumentationMeasurer(
    config,
    log,
    benchmark,
    instrumentor,
    instrumented,
    backup)
  with ScrutinyRegressionDetector
  with TwinningDetector[ScrutinyRegressionResult] {

  def detect(stupidDummyNotTobeUsedBenchmark: PinpointBenchmark): ScrutinyRegressionResult = {
    if (benchmark.pinpointClass == "" || benchmark.pinpointMethod == "") {
      throw new NoPinpointingMethodException(benchmark)
    }
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
      (current, previous) => current match {
        case failure: MeasurementFailure => ScrutinyImmeasurableFailure(benchmark, failure)
        case _ => previous match {
          case failure: MeasurementFailure => ScrutinyImmeasurableFailure(benchmark, failure)
          case _                           => throw new AlgorithmFlowException(this.getClass)
        }
      })
  }

  private lazy val measureCurrent = instrumentAndMeasure(
    method => instrumentor.sandwich(
      method,
      PinpointHarness.javaInstructionCallStart,
      PinpointHarness.javaInstructionCallEnd),
    config.classpathURLs ++ benchmark.classpathURLs)

  private lazy val measurePrevious = super.measurePrevious(
    benchmark.pinpointPrevious,
    benchmark.context,
    backup,
    instrumentAndMeasure(
      method => instrumentor.sandwich(
        method,
        PinpointHarness.javaInstructionCallStart,
        PinpointHarness.javaInstructionCallEnd),
      config.classpathURLs ++ benchmark.classpathURLs :+ benchmark.pinpointPrevious.toURL))

}
