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
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.regression.CIRegressionFailure
import scala.tools.sbs.regression.CIRegressionSuccess
import scala.tools.sbs.measurement.MeasurementFailure

class MethodRegressionDetector(protected val config: Config,
                               protected val log: Log,
                               benchmark: PinpointBenchmark,
                               instrumentor: CodeInstrumentor,
                               instrumentedURL: URL)
  extends ScrutinyRegressionDetector
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

  private lazy val measureCurrent = classpathBasedMeasure(config.classpathURLs ++ benchmark.classpathURLs)

  private lazy val measurePrevious =
    classpathBasedMeasure(benchmark.pinpointPrevious.toURL :: config.classpathURLs ++ benchmark.classpathURLs)

  private def classpathBasedMeasure(classpathURLs: List[URL]): MeasurementResult = {
    instrument(benchmark, classpathURLs)
    PinpointMeasurerFactory(config, log).measure(benchmark, instrumentedURL :: classpathURLs)
  }

  /** Modifies the `pinpointMethod` to set entry and exit time to
   *  {@link scala.tools.sbs.pinpoint.PinpointHarness}'s static fields.
   */
  private def instrument(benchmark: PinpointBenchmark, classpathURLs: List[URL]) {
    val (clazz, method) = instrumentor.getClassAndMethod(
      benchmark.pinpointClass,
      benchmark.pinpointMethod,
      classpathURLs)
    if (method == null) {
      throw new PinpointingMethodNotFoundException(benchmark)
    }
    instrumentor.sandwich(method, PinpointHarness.javaInstructionCallStart, PinpointHarness.javaInstructionCallEnd)
    instrumentor.writeFile(clazz, instrumentedURL)
  }

}
