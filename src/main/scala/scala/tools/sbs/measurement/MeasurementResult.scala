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

trait MeasurementResult {

  def benchmark: Benchmark

  def series: Series

}

case class MeasurementSuccess(benchmark: Benchmark, series: Series)
  extends MeasurementResult {

}

case class MeasurementFailure(benchmark: Benchmark)
  extends MeasurementResult {

}
