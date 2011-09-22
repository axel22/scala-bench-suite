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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.regression.StatisticFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Log

class SteadyHarness extends SubProcessHarness {

  def run(benchmark: Benchmark): MeasurementResult = {
    val statistic = new StatisticFactory(log, config) create 0
    log.info("[Benchmarking steady state]")
    benchmark.init()
    benchmarkRunner run (
      benchmark,
      series => (statistic CoV series) < Constant.STEADY_THREDSHOLD,
      {
        val start = Platform.currentTime
        (1 to config.runs) map (_ => benchmark.run)
        Platform.currentTime - start
      }
    )
  }

}
