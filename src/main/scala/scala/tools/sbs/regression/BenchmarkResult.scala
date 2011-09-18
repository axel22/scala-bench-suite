/*
 * BenchmarkResult
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurementFailure

abstract class BenchmarkResult(confidenceLevel: Int, measurementResult: MeasurementResult) {

  def confidenceLevel(): Int = confidenceLevel

  def measurementResult(): MeasurementResult = measurementResult

}

case class BenchmarkSuccess(override val confidenceLevel: Int, measurementSuccess: MeasurementSuccess)
  extends BenchmarkResult(confidenceLevel, measurementSuccess)

case class ConfidenceIntervalFailure(override val confidenceLevel: Int,
                                     measurementSuccess: MeasurementSuccess,
                                     means: ArrayBuffer[Double],
                                     CI: (Double, Double))
  extends BenchmarkResult(confidenceLevel, measurementSuccess)

case class ANOVAFailure(override val confidenceLevel: Int,
                        measurementSuccess: MeasurementSuccess,
                        means: ArrayBuffer[Double],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double)
  extends BenchmarkResult(confidenceLevel, measurementSuccess)

case class NoPreviousFailure(measurementSuccess: MeasurementSuccess) extends BenchmarkResult(0, measurementSuccess)

case class ImmeasurableFailure(measurementFailure: MeasurementFailure) extends BenchmarkResult(0, measurementFailure)

case class ExceptionFailure(exception: Exception) extends BenchmarkResult(0, null)
