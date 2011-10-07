/*
 * Measurer
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Cretead by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.io.Log
import scala.tools.sbs.benchmark.Benchmark

/** A measurer for a benchmarking. Should have a typical type for benchmarking
 *  on a typical {@link BenchmarkMode}.
 */
trait Measurer extends Runner {

  def run(benchmark: Benchmark): RunResult = measure(benchmark)

  def measure(benchmark: Benchmark): MeasurementResult

}

/** Factory object of {@link Measurer}.
 */
object MeasurerFactory {

  def apply(config: Config, mode: BenchmarkMode): Measurer = mode match {
    case StartUpState => new StartupHarness
    case _            => new SubJVMMeasurer(config, mode)
  }

}
