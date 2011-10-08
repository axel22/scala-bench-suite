/*
 * SubJVMMeasurer
 * 
 * Version
 * 
 * Created September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.BenchmarkMode
import scala.tools.sbs.Config

/** Measures benchmark metric by invoking a new clean JVM.
 */
class SubJVMMeasurer(config: Config, mode: BenchmarkMode) extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark createLog mode
    val subProcessMeasurer = SubProcessMeasurerFactory(mode)
    val invoker = JVMInvokerFactory(log, config)
    val (result, error) = invoker invoke (invoker.command(subProcessMeasurer, benchmark))
    if (error.length > 0) {
      error foreach log.error
      ExceptionFailure(new Exception(error mkString "\n"))
    }
    else {
      dispose(result)
    }
  }

  /** Disposes a xml string to get the {@link MeasurementResult} it represents.
   *
   *  @param result	A `String` contains and xml element.
   *
   *  @return	The corresponding `MeasurementResult`
   */
  def dispose(result: String): MeasurementResult = try {
    val xml = scala.xml.Utility trim (scala.xml.XML loadString result)
    xml match {
      case <MeasurementSuccess>{ _ }</MeasurementSuccess> =>
        MeasurementSuccess(new Series(
          log,
          ArrayBuffer((xml \\ "value") map (_.text.toLong): _*),
          (xml \\ "confidenceLevel").text.toInt))
      case <UnwarmableFailure/> => UnwarmableFailure()
      case <UnreliableFailure/> => UnreliableFailure()
      case <ProcessFailure/> => ProcessFailure()
      case <ExceptionFailure>{ ect }</ExceptionFailure> => ExceptionFailure(new Exception(ect.text))
      case _ => throw new Exception
    }
  }
  catch {
    case e: Exception => {
      log.error("Malformed XML: " + result)
      ProcessFailure()
    }
  }

}
