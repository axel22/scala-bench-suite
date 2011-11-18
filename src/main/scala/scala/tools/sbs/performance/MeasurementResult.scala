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
package performance

import scala.tools.sbs.benchmark.Benchmark

/** Represents a result of a measurement phase of one benchmark on one {@link BenchmarkMode}.
 */
trait MeasurementResult extends RunResult

case class MeasurementSuccess(series: Series) extends RunSuccess with MeasurementResult {

  def toXML = <MeasurementSuccess>{ series.toXML }</MeasurementSuccess>

}

trait MeasurementFailure extends RunFailure with MeasurementResult {

  def reason: String

}

class TimeoutMeasurementFailure extends MeasurementFailure {

  def reason = "Benchmarking timed out"

  def toXML = <TimeoutMeasurementFailure/>

}
class UnwarmableMeasurementFailure extends MeasurementFailure {

  def reason = "Benchmark could not reach steady state"

  def toXML = <UnwarmableMeasurementFailure/>

}

class UnreliableMeasurementFailure extends MeasurementFailure {

  def reason = "Measurement result unreliable"

  def toXML = <UnreliableMeasurementFailure/>

}

case class ProcessMeasurementFailure(exitValue: Int) extends MeasurementFailure {

  def reason = "Error in benchmark process exit value: " + exitValue

  def toXML = <ProcessMeasurementFailure>{ exitValue }</ProcessMeasurementFailure>

}

case class UnsupportedBenchmarkMeasurementFailure(benchmark: Benchmark, mode: BenchmarkMode)
  extends MeasurementFailure {

  def reason = "Benchmark " + benchmark.name + " unsupports benchmarking mode: " + mode.description

  def toXML = <UnsupportedBenchmarkMeasurementFailure/>

}

case class ExceptionMeasurementFailure(e: Exception) extends MeasurementFailure {

  def reason = e.toString + " raised"

  def exception = e

  def toXML =
    <ExceptionMeasurementFailure>{ e.getMessage }</ExceptionMeasurementFailure>
}
