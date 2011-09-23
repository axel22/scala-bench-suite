/*
 * SubProcessHarness
 * 
 * Version
 * 
 * Created on September 21st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.util.Config
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.util.ReportFactory
import scala.tools.sbs.util.LogLevel.LogLevel
import scala.tools.sbs.util.LogFactory
import scala.tools.sbs.util.Log
/**
 * Driver for measurement in a separated JVM.
 * Choose the harness to run and write the result to output stream.
 */
trait SubProcessHarness extends Measurer {

  protected var benchmarkRunner: BenchmarkRunner = _
  protected var log: Log = _
  protected var config: Config = _

  /**
   * Entry point of the new process.
   */
  def main(args: Array[String]): Unit = {

    val settings = rebuildSettings(args)

    config = settings._1
    val benchmark = settings._2
    log = new LogFactory create settings._3
    benchmarkRunner = new BenchmarkRunner(log, config)

    try {
      reportResult(this run benchmark)
    } catch { case e: Exception => reportResult(new ExceptionFailure(e)) }
  }

  /**
   * Rebuild the measuring config from command arguments.
   *
   * @return	The tuple of the rebuilt `Config` and `Benchmark`
   */
  def rebuildSettings(args: Array[String]): (Config, Benchmark, LogLevel) = {

    (null, null, null)
  }

  /**
   * Reports the measurement result to the main process.
   */
  def reportResult(result: MeasurementResult) {
    Console println MeasurementSignal.RESULT_START
    result match {
      case success: MeasurementSuccess => {
        Console println MeasurementSignal.MEASUREMENT_SUCCESS
        success.series foreach (Console println _)
      }
      case failure: MeasurementFailure => {
        Console println failure.reason
        failure match {
          case ext: ExceptionFailure => {
            Console println ext.e
            Console println ext.e.getStackTraceString
          }
        }
      }
    }
  }

}
