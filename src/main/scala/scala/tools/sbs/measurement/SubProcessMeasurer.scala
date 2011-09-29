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

import scala.xml.XML

/** Driver for measurement in a separated JVM.
 *  Choose the harness to run and write the result to output stream.
 */
trait SubProcessMeasurer extends Measurer {

  protected var benchmarkRunner: BenchmarkRunner = _
  protected var config: Config = _
  protected val mode: BenchmarkMode

  /** Entry point of the new process.
   */
  def main(args: Array[String]): Unit = {

    config = Config(args.tail)
    val benchmark = BenchmarkFactory(XML loadString args.head, config)
    log = benchmark createLog mode

    benchmarkRunner = new BenchmarkRunner(log)

    try reportResult(this measure benchmark)
    catch { case e: Exception => reportResult(new ExceptionFailure(e)) }
  }

  /** Reports the measurement result to the main process.
   */
  def reportResult(result: MeasurementResult) = Console println result.toXML

}

object SubProcessMeasurerFactory {

  def apply(mode: BenchmarkMode): SubProcessMeasurer = mode match {
    case SteadyState() => SteadyHarness
    case MemoryUsage() => MemoryHarness
    case _ => throw new Exception("Wrong harness in SubProcessMeasurerFactory")
  }

}
