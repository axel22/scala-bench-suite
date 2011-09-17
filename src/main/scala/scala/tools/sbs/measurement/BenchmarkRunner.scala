/*
 * BenchmarkRunner
 * 
 * Version
 * 
 * Created on September 11th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.lang.Thread.sleep
import scala.compat.Platform
import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.measurement.BenchmarkType.BenchmarkType
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.regression.Statistic

class BenchmarkRunner(log: Log, config: Config, benchmark: Benchmark) {

  /**
   * Warms the benchmark up if necessary and measures the desired metric.
   *
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   *
   * @return	The result if success, otherwies a `String` describes the reason.
   */
  def run(metric: BenchmarkType): Either[MeasurementResult, String] = {

    if (metric == BenchmarkType.MEMORY) {
    } else if (metric == BenchmarkType.STARTUP) {
      log.info("[Benchmarking startup state]")
      if (benchmark.initCommand()) {
        run(
          metric,
          _ => true,
          {
            val start = Platform.currentTime
            benchmark.runCommand()
            Platform.currentTime - start
          }
        )
      } else {
        Right("Benchmark process failed.")
      }
    } else {
    }
  }

}