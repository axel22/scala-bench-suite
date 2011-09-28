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
import scala.tools.sbs.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess

abstract class BenchmarkResult(benchmark: Benchmark)

case class BenchmarkSuccess(benchmark: Benchmark,
                            mode: BenchmarkMode,
                            confidenceInterval: Int,
                            measurementSuccess: MeasurementSuccess) extends BenchmarkResult(benchmark)

abstract class BenchmarkFailure(benchmark: Benchmark) extends BenchmarkResult(benchmark)

case class ConfidenceIntervalFailure(benchmark: Benchmark,
                                     mode: BenchmarkMode,
                                     confidenceLevel: Int,
                                     measurementSuccess: MeasurementSuccess,
                                     means: ArrayBuffer[Double],
                                     CI: (Double, Double)) extends BenchmarkFailure(benchmark)

case class ANOVAFailure(benchmark: Benchmark,
                        mode: BenchmarkMode,
                        confidenceLevel: Int,
                        measurementSuccess: MeasurementSuccess,
                        means: ArrayBuffer[Double],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double) extends BenchmarkFailure(benchmark)

case class CompileFailure(benchmark: Benchmark) extends BenchmarkFailure(benchmark)

case class NoPreviousFailure(benchmark: Benchmark, measurementSuccess: MeasurementSuccess)
  extends BenchmarkFailure(benchmark)

case class ImmeasurableFailure(benchmark: Benchmark, measurementFailure: MeasurementFailure)
  extends BenchmarkFailure(benchmark)

case class ExceptionFailure(benchmark: Benchmark, exception: Exception) extends BenchmarkFailure(benchmark)
