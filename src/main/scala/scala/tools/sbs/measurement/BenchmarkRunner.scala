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
import scala.tools.sbs.io.Log
import scala.tools.sbs.util.Constant.MAX_MEASUREMENT
import scala.tools.sbs.util.Constant.MAX_WARM
import scala.tools.sbs.benchmark.Benchmark

/** Runs and measures metrics of a benchmark in general case.
 */
class BenchmarkRunner(log: Log) {

  /** Warms the benchmark up if necessary and measures the desired metric.
   *
   *  @param	checkWarm	The function checking whether the benchmark has reached steady state
   *  @param measure	The thunk to calculate the desired metric
   *
   *  @return	The result if success, otherwies a `String` describes the reason.
   */
  def run(benchmark: Benchmark, checkWarm: Series => Boolean, measure: => Long): MeasurementResult = {

    var series = new Series(log)
    var unwarmable = false

    val warmMax = benchmark.multiplier * MAX_WARM
    var warmCount = 0
    var measureCount = 0

    def getSeries {
      series.clear()
      for (mul <- 1 to benchmark.multiplier) {
        cleanUp()
        series += measure
        log.verbose("----Measured----  " + series.last)
      }
    }

    while (measureCount < MAX_MEASUREMENT && !series.isReliable) {
      log.verbose("--Start getting a series--")

      getSeries

      warmCount = benchmark.multiplier
      while (warmCount < warmMax && !checkWarm(series)) {
        log.verbose("----Measured----  " + series.last)

        series.remove(0)
        cleanUp()
        series += measure

        warmCount += 1
      }

      if (checkWarm(series)) {
        log.debug("--Reached steady state--")
        unwarmable = false
        getSeries
      } else {
        log.debug("--Unwarmmable--")
        unwarmable = true
        series.clear()
      }
      log.verbose("--End measurement--")
      measureCount += 1
    }

    benchmark.reset()

    if (series.isReliable) {
      MeasurementSuccess(series)
    } else if (unwarmable) {
      UnwarmableFailure()
    } else {
      UnreliableFailure()
    }
  }

  /** Forces the Java gc to clean up the heap.
   */
  def cleanUp() {
    Platform.collectGarbage
    //    System.runFinalization
    //    sleep(100)
    //    Platform.collectGarbage
  }

}
