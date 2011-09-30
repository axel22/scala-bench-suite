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
trait MeasurementResult {

  def toXML: scala.xml.Elem

}

case class MeasurementSuccess(series: Series) extends MeasurementResult {

  def toXML = <MeasurementSuccess>{ series.toXML }</MeasurementSuccess>

}

trait MeasurementFailure extends MeasurementResult {

  def reason: String

}

case class UnwarmableFailure extends MeasurementFailure {

  def reason = "Benchmark could not reach steady state"

  def toXML = <UnwarmableFailure/>

}

case class UnreliableFailure extends MeasurementFailure {

  def reason = "Measurement result unriliable"

  def toXML = <UnreliableFailure/>

}

case class ProcessFailure extends MeasurementFailure {

  def reason = "Measurement sub-process failed to start"

  def toXML = <ProcessFailure/>

}

case class ExceptionFailure(e: Exception) extends MeasurementFailure {

  def reason = "Exception " + e + " raised"

  def exception = e

  def toXML =
    <ExceptionFailure>{ e.toString + "\n" + e.getStackTraceString }</ExceptionFailure>
}
