/*
 * MeasurementHarness
 * 
 * Version
 * 
 * Created on September 21st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.common.ObjectHarness
import scala.tools.sbs.common.RuntimeTypeChecker
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.xml.XML

/** Driver for measurement in a separated JVM.
 *  Choose the harness to run and write the result to output stream.
 */
trait MeasurementHarness[BenchmarkType <: Benchmark] extends ObjectHarness with RuntimeTypeChecker {

  protected var log: Log = null
  protected var seriesAchiever: SeriesAchiever = null
  protected var config: Config = null
  protected def mode: BenchmarkMode

  /** Entry point of the new process.
   */
  def main(args: Array[String]): Unit = {
    config = Config(args.tail.array)
    UI.config = config
    val benchmark = BenchmarkFactory(UI, config, mode) createFrom (XML loadString args.head)
    if (check(benchmark.getClass)) {
      log = benchmark createLog mode
      seriesAchiever = new SeriesAchiever(config, log)
      try reportResult(this measure benchmark.asInstanceOf[BenchmarkType])
      catch { case e: Exception => reportResult(new ExceptionMeasurementFailure(e)) }
    }
    else {
      reportResult(UnsupportedBenchmarkMeasurementFailure(benchmark, mode))
    }
  }

  def measure(benchmark: BenchmarkType): MeasurementResult

}

trait MeasurementHarnessFactory {

  def apply(mode: BenchmarkMode): MeasurementHarness[_]

}

/** Factory object of {@link SubProcessMeasurer}.
 */
object MeasurementHarnessFactory extends MeasurementHarnessFactory {

  def apply(mode: BenchmarkMode): MeasurementHarness[_] = mode match {
    case SteadyState => SteadyHarness
    case MemoryUsage => MemoryHarness
    case _           => throw new AlgorithmFlowException(this.getClass)
  }

}
