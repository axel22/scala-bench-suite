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
package performance

import scala.compat.Platform
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI

/** Runs and measures metrics of a benchmark in general case.
 */
class SeriesAchiever(config: Config, log: Log) {

  /** Warms the benchmark up if necessary and measures the desired metric.
   *
   *  @param	checkWarm	The function checking whether the benchmark has reached steady state
   *  @param measure	The thunk to calculate the desired metric
   *
   *  @return	The result if success, otherwies a `String` describes the reason.
   */
  def achieve(benchmark: PerformanceBenchmark,
          checkWarm: Series => Boolean,
          measure: () => Long,
          newlyAchieve: Boolean = true): MeasurementResult = {

    var series = new Series(config, log)
    var unwarmable = false

    val warmMax = benchmark.measurement * config.warmRepeat
    var warmCount = 0
    var measureCount = 0

    def getSeries {
      series.clear()
      for (_ <- 1 to benchmark.measurement) {
        cleanUp()
        series += measure()
        log.verbose("    Measured      " + series.last)
        UI.verbose("    Measured      " + series.last)
      }
    }

    while (measureCount < config.reMeasurement && !series.isReliable) {
      log.verbose("  Start getting a series  ")
      UI.info("  Start getting a series  ")

      getSeries

      warmCount = benchmark.measurement
      while (warmCount < warmMax && !checkWarm(series)) {
        series.remove(0)
        cleanUp()
        series += measure()
        
        log.verbose("    Measured      " + series.last)
        UI.verbose("    Measured      " + series.last)

        warmCount += 1
      }

      if (checkWarm(series)) {
        UI.info("  Reached steady state  ")
        unwarmable = false
        if (newlyAchieve) getSeries
      }
      else {
        log.info("  [Unwarmmable]  ")
        UI.info("  [Unwarmmable]  ")
        unwarmable = true
        series.clear()
      }
      UI.info("  End measurement  ")
      measureCount += 1
    }

    if (series.isReliable) {
      MeasurementSuccess(series)
    }
    else if (unwarmable) {
      new UnwarmableMeasurementFailure
    }
    else {
      new UnreliableMeasurementFailure
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
