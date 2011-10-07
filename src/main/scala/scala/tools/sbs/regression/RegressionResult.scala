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
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.benchmark.Benchmark

/** Represents the result of a benchmarking (of one benchmark on one {@link BenchmarkMode}).
 */
abstract class RegressionResult(benchmark: Benchmark) extends BenchmarkResult {

  def benchmarkName = benchmark.name

}

case class RegressionSuccess(benchmark: Benchmark,
                             mode: BenchmarkMode,
                             confidenceInterval: Int,
                             measurementSuccess: MeasurementSuccess)
  extends RegressionResult(benchmark) with BenchmarkSuccess

abstract class RegressionFailure(benchmark: Benchmark) extends RegressionResult(benchmark) with BenchmarkFailure

case class ConfidenceIntervalFailure(benchmark: Benchmark,
                                     mode: BenchmarkMode,
                                     confidenceLevel: Int,
                                     measurementSuccess: MeasurementSuccess,
                                     meansAndSD: ((Double, Double), (Double, Double)),
                                     CI: (Double, Double)) extends RegressionFailure(benchmark)

case class ANOVAFailure(benchmark: Benchmark,
                        mode: BenchmarkMode,
                        confidenceLevel: Int,
                        measurementSuccess: MeasurementSuccess,
                        meansAndSD: ArrayBuffer[(Double, Double)],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double) extends RegressionFailure(benchmark)

case class NoPreviousFailure(benchmark: Benchmark, mode: BenchmarkMode, measurementSuccess: MeasurementSuccess)
  extends RegressionFailure(benchmark)

case class ImmeasurableFailure(benchmark: Benchmark, mode: BenchmarkMode, measurementFailure: MeasurementFailure)
  extends RegressionFailure(benchmark)
