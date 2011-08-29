package ndp.scala.benchmarksuite

import java.lang.Thread.sleep

import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform

import ndp.scala.benchmarksuite.regression.Persistor
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.regression.BenchmarkResult
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Constant
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.Report

package object measurement {

  /**
   * The regression report.
   */
  val report = new Report

  /**
   * Loads benchmark class from .class files.
   *
   * @param
   */
  def runBenchmark() {

  }

  /**
   * Calculates the result's statistic metrics.
   * 
   * @param series	The result of benchmarking
   */
  def constructStatistic(log: Log, config: Config, series: BenchmarkResult) {

//    val statistic = new Statistic(log, config)

    val mean = Statistic mean series
    val confidenceInterval = Statistic confidenceInterval series
    val diff = (confidenceInterval.last - confidenceInterval.head) / 2

    for (i <- series) {
      log debug ("[Measured]	" + i)
    }
    log("[Average]	" + mean.formatted("%.2f"))
    log("[Confident Interval]	[" + confidenceInterval.head.formatted("%.2f") + "; " + confidenceInterval.last.formatted("%.2f") + "]")
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
    var storedResult: BenchmarkResult = null
    var line: String = null
//    val statistic = new Statistic(log, config)

    val persistor = new Persistor(log, config)
    persistor += result
    persistor.load

    try {
      if (Statistic testDifference persistor) {
        report(log, config, Constant.FAILED)
      } else {
        report(log, config, Constant.PASS)
      }
    } catch {
//      case e => log debug e.toString
      case e => throw e
    }
  }

}