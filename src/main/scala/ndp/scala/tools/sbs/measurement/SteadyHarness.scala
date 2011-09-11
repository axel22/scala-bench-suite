/*
 * SteadyHarness
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package measurement

import scala.compat.Platform

import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Constant

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
object SteadyHarness extends SubProcessHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
   * <li>Iterates the invoking of benchmark <code>main</code> method for it to reach the steady state.
   * <li>Measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  def run(): Either[BenchmarkResult, String] = {
    log("[Benchmarking steady state]")

    benchmark.init()
    runBenchmark(
      series => (Statistic CoV series) < Constant.STEADY_THREDSHOLD,
      {
        val start = Platform.currentTime
        (1 to config.runs) map (_ => benchmark.run)
        Platform.currentTime - start
      }
    )
  }

}
