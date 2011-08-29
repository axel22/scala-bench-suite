package ndp.scala.benchmarksuite

import java.lang.Thread.sleep

import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform

import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.BenchmarkResult
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
   */
  def constructStatistic(log: Log, series: ArrayBuffer[Long]) {

    val statistic = new Statistic(series)

    val mean = statistic.mean
    val ConfidenceInterval = statistic.ConfidenceInterval
    val diff = (ConfidenceInterval.last - ConfidenceInterval.head) / 2

    for (i <- series) {
      log debug ("[Measured]	" + i)
    }
    log("[Average]	" + mean.formatted("%.2f"))
    log("[Confident Interval]	[" + ConfidenceInterval.head.formatted("%.2f") + "; " + ConfidenceInterval.last.formatted("%.2f") + "]")
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
  def detectRegression(log: Log) {
    var storedResult: BenchmarkResult = null
    var line: String = null
    val statistic = new Statistic
    var persistors: ArrayBuffer[ArrayBuffer[Long]] = new ArrayBuffer[ArrayBuffer[Long]]

    log("Input previous result file, double-enter to stop")
    do {
      line = Console.readLine()
      if (!(line equals "")) {
        try {
          storedResult = new BenchmarkResult(line)
          storedResult.load
          persistors += storedResult.series
        } catch {
          case _ => log("File name incorrect")
        }
      }
    } while (!(line equals ""))

    statistic.persistors = persistors

    try {
      if (statistic testDifference) {
        report(log, Constant.FAILED)
      } else {
        report(log, Constant.PASS)
      }
    } catch {
      case e => log.debug(e toString)
    }
  }

}