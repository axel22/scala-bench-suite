/*
 * StartupHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite
package measurement

import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform
import ndp.scala.benchmarksuite.regression.Persistor
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel

/**
 * Class represent the harness controls the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class StartupHarness(log: Log, config: Config) extends Harness(log, config) {

  /**
   * Does the following:
   * <ul>
   * <li>Creates the operating system process for the benchmark classes to run.
   * <li>Iterates the invoking of new JVM instance loading the benchmark classes to measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  override def run(): BenchmarkResult = {

    log("[Benchmarking startup state]")

    val processBuilder = new ProcessBuilder(
      config.JAVACMD,
      "-cp",
      config.SCALA_LIB,
      config.JAVAPROP,
      "scala.tools.nsc.MainGenericRunner",
      "-classpath",
      config.BENCHMARK_BUILD.path,
      config.CLASSNAME
    )
    
    if (config.LOG_LEVEL == LogLevel.DEBUG) {
      log debug processBuilder.command.toString
    }
    
    var result: BenchmarkResult = new BenchmarkResult

    // Ignore the first launch due to system status changing
    var process = processBuilder.start
    process.waitFor

    runBenchmark(
      log,
      config,
      _ => true,
      {
        val start = Platform.currentTime
        process = processBuilder.start
        process.waitFor
        val end = Platform.currentTime
        end - start
      }
    )
  }

}
