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

trait MeasurementResult {

  def toXML: scala.xml.Elem

}

case class MeasurementSuccess(series: Series) extends MeasurementResult {

  def toXML =
    <MeasurementSuccess>
      { series.toXML }
    </MeasurementSuccess>

}

trait MeasurementFailure extends MeasurementResult {

  def reason: String

}

case class UnwarmableFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_UNWARMABLE

  def toXML =
    <UnwarmableFailure>
      <reason>{ reason }</reason>
    </UnwarmableFailure>

}

case class UnreliableFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_UNRELIABLE

  def toXML =
    <UnreliableFailure>
      <reason>{ reason }</reason>
    </UnreliableFailure>

}

case class ProcessFailure extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_PROCESS_FAIL

  def toXML =
    <ProcessFailure>
      <reason>{ reason }</reason>
    </ProcessFailure>

}

case class ExceptionFailure(e: Exception) extends MeasurementFailure {

  def reason = MeasurementSignal.MEASUREMENT_FAILURE_EXCEPTION

  def exception = e

  def toXML =
    <ExceptionFailure>
      <reason>{ reason }</reason>
      <exception>{ e.toString + "\n" + e.getStackTraceString }</exception>
    </ExceptionFailure>
}
