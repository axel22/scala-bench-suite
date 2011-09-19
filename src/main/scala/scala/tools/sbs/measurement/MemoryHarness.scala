/*
 * MemoryHarness
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class MemoryHarness(log: Log, config: Config) extends Harness(log, config) {

  def run(benchmark: Benchmark): MeasurementResult = {
    log.info("[Benchmarking memory consumption]")
    val runtime: Runtime = Runtime.getRuntime
    benchmarkRunner run (
      benchmark,
      series => series forall (_ == series.head),
      {
        val start = runtime.freeMemory
        benchmark.init()
        benchmark.run()
        start - runtime.freeMemory
      }
    )
  }

}
