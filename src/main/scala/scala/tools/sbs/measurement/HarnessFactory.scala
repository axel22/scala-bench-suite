/*
 * HarnessFactory
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.BenchmarkMode.MEMORY
import scala.tools.sbs.benchmark.BenchmarkMode.STARTUP
import scala.tools.sbs.benchmark.BenchmarkMode.STEADY
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode

class HarnessFactory(log: Log, config: Config) {

  def run(mode: BenchmarkMode): Harness = mode match {
    case STEADY => new SteadyHarness(log, config)
    case MEMORY => new MemoryHarness(log, config)
    case STARTUP => new StartupHarness(log, config)
  }

}
