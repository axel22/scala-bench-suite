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

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess

/** Represents the result of a benchmarking (of one benchmark on one {@link BenchmarkMode}).
 */
abstract class BenchmarkResult(benchmark: Benchmark) {

  def benchmark: Benchmark

}

case class BenchmarkSuccess(benchmark: Benchmark,
                            mode: BenchmarkMode,
                            confidenceInterval: Int,
                            measurementSuccess: MeasurementSuccess) extends BenchmarkResult(benchmark)

abstract class BenchmarkFailure(benchmark: Benchmark) extends BenchmarkResult(benchmark)

case class ConfidenceIntervalFailure(benchmark: Benchmark,
                                     mode: BenchmarkMode,
                                     confidenceLevel: Int,
                                     measurementSuccess: MeasurementSuccess,
                                     meansAndSD: ((Double, Double), (Double, Double)),
                                     CI: (Double, Double)) extends BenchmarkFailure(benchmark)

case class ANOVAFailure(benchmark: Benchmark,
                        mode: BenchmarkMode,
                        confidenceLevel: Int,
                        measurementSuccess: MeasurementSuccess,
                        meansAndSD: ArrayBuffer[(Double, Double)],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double) extends BenchmarkFailure(benchmark)

case class CompileFailure(benchmark: Benchmark) extends BenchmarkFailure(benchmark)

case class NoPreviousFailure(benchmark: Benchmark, mode: BenchmarkMode, measurementSuccess: MeasurementSuccess)
  extends BenchmarkFailure(benchmark)

case class ImmeasurableFailure(benchmark: Benchmark, mode: BenchmarkMode, measurementFailure: MeasurementFailure)
  extends BenchmarkFailure(benchmark)

case class ExceptionFailure(benchmark: Benchmark, mode: BenchmarkMode, exception: Exception)
  extends BenchmarkFailure(benchmark)
