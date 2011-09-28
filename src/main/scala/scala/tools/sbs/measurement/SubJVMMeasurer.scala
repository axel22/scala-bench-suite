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
import scala.tools.sbs.io.UI
import scala.xml.XML

import BenchmarkMode.BenchmarkMode

/** Measures benchmark metric by invoking a new clean JVM
 */
class SubJVMMeasurer(config: Config, mode: BenchmarkMode) extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark.log
    val subProcessMeasurer = SubProcessMeasurerFactory(mode)
    val (logAndResult, error) = JVMInvokerFactory(log, config).invoke(subProcessMeasurer, benchmark)
    dispose(logAndResult, error)
  }

  def dispose(logAndResult: ArrayBuffer[String], error: ArrayBuffer[String]): MeasurementResult = {

    logAndResult.init foreach UI.apply

    if (error.length > 0) {
      error foreach log.error
      ExceptionFailure(new Exception(error mkString "\n"))
    } else {
      val xml = XML loadString logAndResult.last
      scala.xml.Utility.trim(xml) match {
        case <MeasurementSuccess><Series><confidenceLevel>{ confidenceLevel }</confidenceLevel><data>{ valueNodeSeq@_* }</data></Series></MeasurementSuccess> =>
          try {
            val data = ArrayBuffer(
              (for (valueNode <- valueNodeSeq) yield valueNode match {
                case <value>{ value }</value> => value.text.toLong
                case _ => -1
              }): _*)
            if (data forall (_ != -1)) {
              MeasurementSuccess(new Series(log, data, confidenceLevel.text.toInt))
            } else {
              ProcessFailure()
            }
          } catch {
            case e: Exception => {
              log.error("Malformed XML: " + xml)
              ExceptionFailure(e)
            }
          }
        case <UnwarmableFailure/> => UnwarmableFailure()
        case <UnreliableFailure/> => UnreliableFailure()
        case <ProcessFailure/> => ProcessFailure()
        case <ExceptionFailure>{ ect }</ExceptionFailure> => ExceptionFailure(new Exception(ect.text))
        case _ => {
          log.error("Malformed XML: " + xml)
          ProcessFailure()
        }
      }
    }
  }

}
