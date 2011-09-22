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

abstract class MeasurementResult

case class MeasurementSuccess(series: Series) extends MeasurementResult

abstract class MeasurementFailure extends MeasurementResult {

  def reason: String

}

case class UnwarmableFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_UNWARMABLE

}

case class UnreliableFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_UNRELIABLE

}

case class ProcessFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_PROCESS_FAIL

}

case class ExceptionFailure(e: Exception) extends MeasurementFailure {
  
  def reason = MeasurementSignal.MEASUREMENT_FAILURE_EXCEPTION
  
  def exception = e
}
