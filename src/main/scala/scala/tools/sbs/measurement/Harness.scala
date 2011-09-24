/*
 * Harness
 * 
 * Version
 * 
 * Created September 24th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.io.Log
import scala.tools.sbs.regression.BenchmarkResult

trait Harness extends Measurer {

  def run(benchmark: Benchmark): BenchmarkResult

}

object HarnessFactory {

  def apply(log: Log, config: Config, mode: BenchmarkMode): Harness = mode match {
    case BenchmarkMode.STEADY => new SteadyHarness
    case BenchmarkMode.MEMORY => new MemoryHarness
    case BenchmarkMode.STARTUP => new StartupHarness(log, config)
  }

}
