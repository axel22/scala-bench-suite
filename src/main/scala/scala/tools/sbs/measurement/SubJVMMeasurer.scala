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
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log

class SubJVMMeasurer(log: Log, config: Config) extends Measurer {

  def measure(benchmark: Benchmark, mode: BenchmarkMode): MeasurementResult = {
    val subProcessMeasurer = SubProcessMeasurerFactory(mode)
    dispose(JVMInvokerFactory(log, config).invoke(subProcessMeasurer, benchmark))
  }

  def dispose(logAndResult: ArrayBuffer[String]): MeasurementResult = {
    null
  }

}