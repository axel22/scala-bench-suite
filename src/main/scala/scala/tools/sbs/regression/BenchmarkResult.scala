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

abstract class BenchmarkResult(benchmark: Benchmark)

case class BenchmarkSuccess(benchmark: Benchmark, persistor: Persistor, confidenceLevel: Int)
  extends BenchmarkResult(benchmark: Benchmark)

case class ConfidenceIntervalFailure(benchmark: Benchmark,
                                     persistor: Persistor,
                                     means: ArrayBuffer[Double],
                                     CI: (Double, Double),
                                     confidenceLevel: Int)
  extends BenchmarkResult(benchmark: Benchmark)

case class ANOVAFailure(benchmark: Benchmark,
                        persistor: Persistor,
                        means: ArrayBuffer[Double],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double,
                        confidenceLevel: Int)
  extends BenchmarkResult(benchmark: Benchmark)

case class NoPreviousFailure(benchmark: Benchmark, persistor: Persistor) extends BenchmarkResult(benchmark)

case class ImmeasurableFailure(benchmark: Benchmark) extends BenchmarkResult(benchmark)

case class ExceptionFailure(benchmark: Benchmark, e: Exception) extends BenchmarkResult(benchmark)
