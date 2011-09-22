/*
 * StartupHarness
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
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class StartupHarness(log: Log, config: Config) extends Measurer {

  def run(benchmark: Benchmark): MeasurementResult = {
    log.info("[Benchmarking startup state]")
    if (benchmark.initCommand()) {
      val benchmarkRunner = new BenchmarkRunner(log, config)
      benchmarkRunner run (
        benchmark,
        _ => true,
        {
          val start = Platform.currentTime
          benchmark.runCommand()
          Platform.currentTime - start
        }
      )
    } else {
      ProcessFailure()
    }
  }

}
