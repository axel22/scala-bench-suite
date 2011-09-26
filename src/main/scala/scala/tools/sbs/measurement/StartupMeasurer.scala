/*
 * StartupMeasurer
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
import scala.tools.sbs.io.Log

class StartupMeasurer(config: Config) extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark.log
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
        })
    } else {
      ProcessFailure()
    }
  }

}
