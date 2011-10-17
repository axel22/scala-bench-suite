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

  /** Measures the desired metric from benchmark.
   *  Measurements are achieved through `Benchmark.run()` method.
   *  For benchmarks that need no initialization,
   *   the `runs` argument can be any positive integer value.
   *  On the other hand, benchmarks that need initializations,
   *  which had been defined as subclass of
   *  {@link scala.tools.sbs.benchmark.BenchmarkTemplate}
   *  should have `runs` argument equals to 1.
   *  (The `Benchmark.init()` method will be run only once
   *  between two measurements, so if `runs` is larger than 1,
   *  the benchmark may fail to run.)
   */
  def measure(benchmark: Benchmark): MeasurementResult

}

/** Factory object of {@link Measurer}.
 */
object MeasurerFactory {

  def apply(config: Config, mode: BenchmarkMode): Measurer = mode match {
    case StartUpState => new StartupHarness(config)
    case _            => new SubJVMMeasurer(config, mode)
  }

}
