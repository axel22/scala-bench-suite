/*
 * package ndp.scala.tools.sbs.measurement
 * 
 * Version
 * 
 * Created on September 5th, 2011
 * 
 * Created by ND P
 */
package ndp.scala.tools.sbs

import java.lang.Thread.sleep
import scala.compat.Platform

import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.UI

package object measurement {

  /**
   *
   */
  def rebuildSettings(args: Array[String]): (Config, Log) = {
    val confArgs = args take Constant.MAX_ARGUMENT_CONFIG
    val logArgs = args slice (Constant.MAX_ARGUMENT_CONFIG, args.length)

    for (c <- confArgs) {
      UI("Config " + c)
    }
    for (l <- logArgs) {
      UI("Log    " + l)
    }

    config = new Config(confArgs)
    log = new Log(logArgs)
    
    (config, log)
  }

  /**
   * Warms the benchmark up and measures the desire metric.
   *
   * @param log	The logger
   * @param config
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   */
  def runBenchmark(checkWarm: BenchmarkResult => Boolean, measure: => Long): BenchmarkResult = {

    log.verbose("[Warmup]")

    var result = new BenchmarkResult

    for (mul <- 1 to config.multiplier) {
      cleanUp
      result += measure

      log.verbose("[Measured]	" + result.last)
    }

    while (!checkWarm(result)) {
      cleanUp

      log.verbose("[Measured]	" + result.last)

      result.remove(0)
      result += measure
    }

    log.verbose("[End measurement]")

    constructStatistic(log, config, result)

    log.verbose("[End constructing statistical metrics]")

    result
  }

  /**
   * Calculates the result's statistical metrics.
   *
   * @param series	The result of benchmarking
   */
  def constructStatistic(log: Log, config: Config, result: BenchmarkResult) {

    val mean = Statistic mean result
    val (left, right) = Statistic confidenceInterval result
    val diff = (right - left) / 2

    for (i <- result) {
      log.debug("[Measured]	" + i)
    }
    log("[Average]	            " + (mean formatted "%.2f"))
    log("[Confident Interval]	[" + (left formatted "%.2f") + "; " + (right formatted "%.2f") + "]")
    log("[Difference]           " + (diff formatted "%.2f") + " = " + ((diff / mean * 100) formatted "%.2f") + "%")
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

}