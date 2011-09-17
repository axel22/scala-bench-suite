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

import scala.compat.Platform
import scala.tools.sbs.measurement.BenchmarkType.BenchmarkType
import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Log

class MemoryHarness(log: Log, config: Config) extends Harness(log: Log, config: Config) {

  def run(benchmark: Benchmark): MeasurementResult = {
    log.info("[Benchmarking memory consumption]")
    val runtime: Runtime = Runtime.getRuntime
    run(
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