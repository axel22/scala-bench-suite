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
import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Log

class SteadyHarness(log: Log, config: Config)
  extends Harness(log: Log, config: Config) {

  def run(benchmark: Benchmark): MeasurementResult = {
    val statistic = new Statistic(log, config, 0)
    log.info("[Benchmarking steady state]")
    benchmark.init()
    run(
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
