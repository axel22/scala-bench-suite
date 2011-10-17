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

/** Represents a result of a measurement phase of one benchmark on one {@link BenchmarkMode}.
 */
trait MeasurementResult extends RunResult {

  def toXML: scala.xml.Elem

}

case class MeasurementSuccess(series: Series) extends RunSuccess with MeasurementResult {

  def toXML = <MeasurementSuccess>{ series.toXML }</MeasurementSuccess>

}

trait MeasurementFailure extends RunFailure with MeasurementResult {

  def reason: String

}

class UnwarmableFailure extends MeasurementFailure {

  def reason = "Benchmark could not reach steady state"

  def toXML = <UnwarmableFailure/>

}

class UnreliableFailure extends MeasurementFailure {

  def reason = "Measurement result unriliable"

  def toXML = <UnreliableFailure/>

}

class ProcessFailure extends MeasurementFailure {

  def reason = "Measurement sub-process failed to start"

  def toXML = <ProcessFailure/>

}

case class ExceptionFailure(e: Exception) extends MeasurementFailure {

  def reason = "Exception " + e + " raised"

  def exception = e

  def toXML =
    <ExceptionFailure>{ e.toString + "\n" + e.getStackTraceString }</ExceptionFailure>
}
