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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.common.JVMInvokerFactory
import scala.sys.process.Process

/** Measurer for benchmarking on startup state.
 */
class StartupHarness(config: Config) extends Measurer {

  def measure(benchmark: Benchmark): MeasurementResult = {
    log = benchmark createLog StartUpState
    log.info("[Benchmarking startup state]")

    val command = JVMInvokerFactory(log, config) command benchmark
    val process = Process(command)
    
    if (process.! == 0) {
      val benchmarkRunner = new BenchmarkRunner(log)
      benchmarkRunner run (
        benchmark,
        _ => true,
        {
          val start = Platform.currentTime
          process.!
          Platform.currentTime - start
        })
    }
    else {
      new ProcessFailure
    }
  }

}
