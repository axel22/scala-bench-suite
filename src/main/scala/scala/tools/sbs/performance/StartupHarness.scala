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
package performance

import scala.compat.Platform
import scala.sys.process.Process
import scala.tools.sbs.common.JVMInvokerFactory
import scala.tools.sbs.io.Log

/** Measurer for benchmarking on startup state.
 */
class StartupHarness(protected val log: Log, protected val config: Config) extends Measurer {

  override protected val mode: BenchmarkMode = StartUpState

  def measure(benchmark: PerformanceBenchmark): MeasurementResult = {
    log.info("[Benchmarking startup state]")

    val command = JVMInvokerFactory(log, config).command(benchmark, config.classpathURLs ++ benchmark.classpathURLs)
    val process = Process(command)
    val exitValue = process !

    if (exitValue == 0) {
      new SeriesAchiever(config, log) achieve (
        benchmark,
        _ => true,
        () => {
          val start = Platform.currentTime
          process.!
          Platform.currentTime - start
        },
        false)
    }
    else {
      new ProcessMeasurementFailure(exitValue)
    }
  }

}
