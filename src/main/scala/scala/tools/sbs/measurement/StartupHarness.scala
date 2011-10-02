/*
 * StartupHarness
 * 
 * Version
 * 
 * Created on September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.compat.Platform
import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.common.StartUpState

/** Measurer for benchmarking on startup state.
 */
class StartupHarness extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark createLog StartUpState()
    log.info("[Benchmarking startup state]")

    if (benchmark.initCommand()) {
      val benchmarkRunner = new BenchmarkRunner(log)
      benchmarkRunner run (
        benchmark,
        _ => true,
        {
          val start = Platform.currentTime
          benchmark.runCommand()
          Platform.currentTime - start
        })
    } else {
      ProcessFailure()
    }
  }

}
