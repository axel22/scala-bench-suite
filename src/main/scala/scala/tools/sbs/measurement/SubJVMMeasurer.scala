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
import scala.xml.XML

import BenchmarkMode.BenchmarkMode

/** Measures benchmark metric by invoking a new clean JVM
 */
class SubJVMMeasurer(config: Config, mode: BenchmarkMode) extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark.log
    val subProcessMeasurer = SubProcessMeasurerFactory(mode)
    val (result, error) = JVMInvokerFactory(log, config).invoke(subProcessMeasurer, benchmark)
    dispose(result, error)
  }

  def dispose(result: String, error: ArrayBuffer[String]): MeasurementResult = {
    if (error.length > 0) {
      error foreach log.error
      ExceptionFailure(new Exception(error mkString "\n"))
    } else {
      val xml = XML loadString result
      xml match {
        case <MeasurementSuccess>
               <Series>
                 <confidenceLevel>{ confidenceLevel }</confidenceLevel>
                 <data>{ valueNodeSeq@_* }</data>
               </Series>
             </MeasurementSuccess> => try {
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
        case _ => {
          log.error("Malformed XML: " + xml)
          ProcessFailure()
        }
      }
    }
  }

}
