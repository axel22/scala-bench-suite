/*
 * BenchmarkRunner
 * 
 * Version
 * 
 * Created on September 11th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.lang.Thread.sleep
import java.lang.System

import scala.compat.Platform
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.util.Constant

class BenchmarkRunner(log: Log, config: Config) {

  /** Warms the benchmark up if necessary and measures the desired metric.
   *
   *  @param	checkWarm	The function checking whether the benchmark has reached steady state
   *  @param measure	The thunk to calculate the desired metric
   *
   *  @return	The result if success, otherwies a `String` describes the reason.
   */
  def run(benchmark: Benchmark, checkWarm: Series => Boolean, measure: => Long): MeasurementResult = {

    log.verbose("")
    log.verbose("--Warmup--")

    var series = new Series(log, config)
    var unwarmable = false

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
        unwarmable = true
        series.clear()
      } else {
        unwarmable = false
      }

      iteratorMeasure += 1
      log.verbose("[End measurement]")
    }

    benchmark.reset()

    if (iteratorMeasure >= Constant.MAX_MEASUREMENT && !series.isReliable) {
      if (unwarmable) {
        UnwarmableFailure()
      } else {
        UnreliableFailure()
      }
    } else {
      MeasurementSuccess(series)
    }
  }

  /** Forces the Java gc to clean up the heap.
   */
  def cleanUp() {
    Platform.collectGarbage
    System.runFinalization
    sleep(100)
    Platform.collectGarbage
  }

}
