/*
 * StartupHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import scala.compat.Platform
import scala.collection.mutable.ArrayBuffer
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.BenchmarkResult
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log

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

    val processBuilder = new ProcessBuilder("scala.bat", "-classpath", config.CLASSPATH, config.CLASSNAME)

    var start: Long = 0
    var end: Long = 0
    var series: ArrayBuffer[Long] = new ArrayBuffer
    var result: BenchmarkResult = null
    
    // Ignore the first launch due to system status changing
    var process = processBuilder.start
    process.waitFor

    for (i <- 1 to config.MULTIPLIER) {
      start = Platform.currentTime
      process = processBuilder.start
      process.waitFor
      end = Platform.currentTime
      series += end - start
    }

    constructStatistic(log, series)

    result = new BenchmarkResult(series, config.CLASSNAME, true)
    result.storeByDefault
    result
  }

}
