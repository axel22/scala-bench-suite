package ndp.scala.benchmarksuite

import java.lang.Thread.sleep
import scala.compat.Platform
import ndp.scala.benchmarksuite.regression.BenchmarkResult
import ndp.scala.benchmarksuite.regression.Persistor
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Constant
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.Report
import scala.collection.mutable.ArrayBuffer

package object measurement {

  /**
   * Warms the benchmark up and measures the desire metric.
   *
   * @param log	The logger
   * @param config
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   */
  def runBenchmark(log: Log, config: Config, checkWarm: (BenchmarkResult) => Boolean, measure: => Long): BenchmarkResult = {

    log verbose "[Warmup]"

    var result = new BenchmarkResult

    for (mul <- 1 to config.MULTIPLIER) {
      cleanUp
      result += measure

      log verbose "[Measured]	" + result.last
    }

    while (!checkWarm(result)) {
      cleanUp

      log verbose "[Measured]	" + result.last

      result remove 0
      result += measure
    }

    log verbose "[End measurement]"

    constructStatistic(log, config, result)

    log verbose "[End constructing statistical metrics]"

    detectRegression(log, config, result)

    log verbose "[End detecting regression]"

    (new Persistor(log, config) += result).store

    result
  }

  /**
   * Calculates the result's statistical metrics.
   *
   * @param series	The result of benchmarking
   */
  def constructStatistic(log: Log, config: Config, series: BenchmarkResult) {

    val mean = Statistic mean series
    val confidenceInterval = Statistic confidenceInterval series
    val diff = (confidenceInterval.last - confidenceInterval.head) / 2

    for (i <- series) {
      log debug ("[Measured]	" + i)
    }
    log("[Average]	" + mean.formatted("%.2f"))
    log("[Confident Interval]	[" + confidenceInterval.head.formatted("%.2f") + "; " + (confidenceInterval.last formatted "%.2f") + "]")
    log("[Difference] " + diff.formatted("%.2f") + " = " + (diff / mean * 100).formatted("%.2f") + "%")
  }

  /**
   * Forces the Java gc to clean up the heap.
   */
  def cleanUp() {
    Platform.collectGarbage
    System.runFinalization
    sleep(100)
    Platform.collectGarbage
  }

  /**
   * Loads benchmark histories from files and uses <code>Statistic</code> class to detect regression.
   */
  def detectRegression(log: Log, config: Config, result: BenchmarkResult) {

    val report = new Report
    var storedResult: BenchmarkResult = null
    var line: String = null
    //    val statistic = new Statistic(log, config)

    val persistor = new Persistor(log, config)
    persistor += result
    persistor.load

    if (Statistic testDifference persistor) {
      val means: ArrayBuffer[Double] = new ArrayBuffer[Double]
      log debug persistor.toString
      for (i <- persistor) {
        means += Statistic mean i
      }
      log debug means.toString
      report(log, config, Constant.FAILED, Report dueToRegression means)
    } else {
      report(log, config, Constant.PASS, null)
    }
  }

}