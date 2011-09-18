/*
 * Harness
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.lang.Thread.sleep

import scala.compat.Platform
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Log

abstract class Harness(log: Log, config: Config) extends Measurer {

  def run(benchmark: Benchmark): MeasurementResult

  def run(benchmark: Benchmark, checkWarm: Series => Boolean, measure: => Long): MeasurementResult = {

    log.verbose("")
    log.verbose("--Warmup--")

    var series = new SeriesFactory(log, config) create
    var falureReason = "Benchmark irreliable"

    val iteratorMax = config.multiplier * 5
    var iteratorCount = 0
    var iteratorMeasure = 0

    while (iteratorMeasure < Constant.MAX_MEASUREMENT && !series.isReliable) {

      series.clear()

      log.verbose("")
      log.verbose("--Start getting a series--")

      for (mul <- 1 to config.multiplier) {
        cleanUp()
        series += measure
        log.verbose("----Measured----  " + series.last)
      }
      iteratorCount = config.multiplier

      while (iteratorCount < iteratorMax && !checkWarm(series)) {
        log.verbose("----Measured----  " + series.last)

        cleanUp()
        series.remove(0)
        series += measure
        iteratorCount += 1
      }

      if (iteratorCount == iteratorMax) {
        log.verbose("--Unwarmmable--")
        falureReason = "Cannot warm the benchmark up"
        series.clear()
      }

      iteratorMeasure += 1
      log.verbose("[End measurement]")
    }

    benchmark.finallize()

    if (iteratorMeasure >= Constant.MAX_MEASUREMENT) {
      MeasurementFailure(benchmark, series, falureReason)
    } else {
      MeasurementSuccess(benchmark, series)
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

