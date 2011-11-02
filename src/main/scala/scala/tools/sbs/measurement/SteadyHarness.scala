/*
 * SteadyHarness
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
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.util.Constant.STEADY_THRESHOLD
import scala.tools.sbs.benchmark.Benchmark

/** Measurer for benchmarking on steady state. Should be run on a clean new JVM.
 */
object SteadyHarness extends MeasurementHarness[PerformanceBenchmark] {

  protected val mode = SteadyState
  
  protected val upperBound = manifest[PerformanceBenchmark]

  def measure(benchmark: PerformanceBenchmark): MeasurementResult = {
    val statistic = StatisticsFactory(log)
    log.info("[Benchmarking steady state]")
    benchmarkRunner run (
      benchmark,
      series => (statistic CoV series) < STEADY_THRESHOLD,
      {
        benchmark.init()
        val start = Platform.currentTime
        var i = 0
        while (i < benchmark.runs) {
          benchmark.run()
          i += 1
        }
        val measured = Platform.currentTime - start
        benchmark.reset()
        measured
      })
  }

}
