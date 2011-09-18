/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.Benchmark

abstract class MeasurementResult(benchmark: Benchmark, series: Series) {

  def benchmark(): Benchmark = benchmark

  def series(): Series = series

}

case class MeasurementSuccess(benchmark: Benchmark, series: Series) extends MeasurementResult(benchmark, series)

case class MeasurementFailure(benchmark: Benchmark, series: Series, reason: String)
  extends MeasurementResult(benchmark, series) 
