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
import scala.tools.sbs.measurement.Benchmark

abstract class BenchmarkResult(benchmark: Benchmark, persistor: Persistor)

case class BenchmarkSuccess(benchmark: Benchmark, persistor: Persistor)
  extends BenchmarkResult(benchmark: Benchmark, persistor: Persistor) {

}

case class ConfidenceIntervalFailure(benchmark: Benchmark,
                                     persistor: Persistor,
                                     means: ArrayBuffer[Double],
                                     CI: (Double, Double))
  extends BenchmarkResult(benchmark: Benchmark, persistor: Persistor) {

}

case class ANOVAFailure(benchmark: Benchmark,
                        persistor: Persistor,
                        means: ArrayBuffer[Double],
                        SSA: Double,
                        SSE: Double,
                        FValue: Double,
                        F: Double)
  extends BenchmarkResult(benchmark: Benchmark, persistor: Persistor) {

}
