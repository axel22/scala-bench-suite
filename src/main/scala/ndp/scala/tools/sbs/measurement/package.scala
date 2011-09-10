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

import ndp.scala.tools.sbs.util.Constant

package object measurement {

  /**
   * Warms the benchmark up and measures the desire metric.
   *
   * @param log	The logger
   * @param config
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   */
  def runBenchmark(checkWarm: BenchmarkResult => Boolean, measure: => Long): Either[BenchmarkResult, String] = {

    val endl = System getProperty "line.separator"
    log.verbose("")
    log.verbose("--Warmup--")

    var result = new BenchmarkResult

    val iteratorMax = config.multiplier * 5
    var iteratorCount = 0
    var iteratorMeasure = 0

    while (iteratorMeasure < Constant.MAX_MEASUREMENT && !result.isReliable) {

      result.clear()

      log.verbose("")
      log.verbose("--Start getting a series--")

      for (mul <- 1 to config.multiplier) {
        cleanUp
        result += measure

        log.verbose("----Measured----  " + result.last)
      }
      iteratorCount = config.multiplier

      while ((iteratorCount < iteratorMax) && (!checkWarm(result))) {
        cleanUp

        log.verbose("----Measured----  " + result.last)

        result.remove(0)
        result += measure
        iteratorCount += 1
      }

      if (iteratorCount == iteratorMax) {
        log.verbose("--Unwarmmable--")
        result.clear()
      }

      iteratorMeasure += 1
      log.verbose("[End measurement]")
    }

    benchmark.finallize()

    if (iteratorMeasure >= Constant.MAX_MEASUREMENT) {
      Right("Immeasurable")
    } else {
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