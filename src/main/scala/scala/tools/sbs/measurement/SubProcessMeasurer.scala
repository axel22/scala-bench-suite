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

import scala.tools.sbs.io.Log
import scala.tools.sbs.util.XMLUtil
import scala.xml.XML

import BenchmarkMode.BenchmarkMode

/** Driver for measurement in a separated JVM.
 *  Choose the harness to run and write the result to output stream.
 */
trait SubProcessMeasurer extends Measurer {

  protected var benchmarkRunner: BenchmarkRunner = _
  protected var config: Config = _

  /** Entry point of the new process.
   */
  def main(args: Array[String]): Unit = {

    val settings = rebuildSettings(args)

    config = settings._1
    val benchmark = settings._2
    log = benchmark.log

    benchmarkRunner = new BenchmarkRunner(log, config)

    try reportResult(this measure benchmark)
    catch { case e: Exception => reportResult(new ExceptionFailure(e)) }
  }

  /** Rebuild the measuring config from command arguments.
   *
   *  @return	The tuple of the rebuilt `Config` and `Benchmark`
   */
  def rebuildSettings(args: Array[String]): (Config, Benchmark) = {
    val config = XMLUtil.XMLToConfig(XML loadString args(0))
    val benchmark = XMLUtil.XMLToBenchmark(XML loadString args(1), config)
    (config, benchmark)
  }

  /** Reports the measurement result to the main process.
   */
  def reportResult(result: MeasurementResult) = Console println result.toXML

}

object SubProcessMeasurerFactory {

  def apply(mode: BenchmarkMode): SubProcessMeasurer = mode match {
    case BenchmarkMode.STEADY => new SteadyHarness
    case BenchmarkMode.MEMORY => new MemoryHarness
    case _ => throw new Exception("Wrong harness in SubProcessMeasurerFactory")
  }

}
