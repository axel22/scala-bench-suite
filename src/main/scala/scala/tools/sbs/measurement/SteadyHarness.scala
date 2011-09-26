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
import scala.tools.sbs.util.Constant

class SteadyHarness extends SubProcessMeasurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    val statistic = StatisticsFactory(log)
    log.info("[Benchmarking steady state]")
    benchmark.init()
    benchmarkRunner run (
      benchmark,
      series => (statistic CoV series) < Constant.STEADY_THREDSHOLD,
      {
        val start = Platform.currentTime
        var i = 0
        while (i < benchmark.runs) {
          benchmark.run()
          i += 1
        }
        Platform.currentTime - start
      })
  }

}
