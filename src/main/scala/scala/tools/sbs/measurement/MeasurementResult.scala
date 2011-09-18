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

abstract class MeasurementResult(series: Series) {
  
  def series(): Series = series
  
}

case class MeasurementSuccess(override val series: Series) extends MeasurementResult(series)

abstract class MeasurementFailure(override val series: Series) extends MeasurementResult(series) {

  def reason: String

}

case class UnwarmableFailure(override val series: Series) extends MeasurementFailure(series) {

  def reason = "Benchmark could not reach steady state"

}

case class UnreliableFailure(override val series: Series) extends MeasurementFailure(series) {

  def reason = "Measured metric is unreliable"

}

case class ProcessFailure extends MeasurementFailure(null) {

  def reason = "----Benchmark process could not run"

}
