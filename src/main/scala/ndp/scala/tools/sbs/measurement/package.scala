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
  def runBenchmark(checkWarm: BenchmarkResult => Boolean, measure: => Long): Either[BenchmarkResult, String] = {

    log.verbose("[Warmup]")

    var result = new BenchmarkResult

    val iteratorMax = config.multiplier * 5
    var iteratorCount = 0
    var measureCount = 0

    while ((measureCount < Constant.MAX_MEASUREMENT) && (!result.isReliable)) {
      
      Statistic.resetConfidenceInterval()
      
      while (Statistic.isConfidenceLevelAcceptable) {

        log.verbose("[Start getting a series]")

        for (mul <- 1 to config.multiplier) {
          cleanUp
          result += measure

          log.verbose("[Measured]	" + result.last)
        }
        iteratorCount = config.multiplier

        while ((iteratorCount < iteratorMax) && (!checkWarm(result))) {
          cleanUp

          log.verbose("[Measured]	" + result.last)

          result.remove(0)
          result += measure
          iteratorCount += 1
        }
        if (iteratorCount == iteratorMax) {
          Statistic.reduceConfidenceLevel()
        }
      }

      measureCount += 1
      log.verbose("[End measurement]")
    }

    log.verbose("[End constructing statistical metrics]")

    if (measureCount >= Constant.MAX_MEASUREMENT) {
      Right("Immeasurable")
    }
    else {
      Left(result)
    }
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