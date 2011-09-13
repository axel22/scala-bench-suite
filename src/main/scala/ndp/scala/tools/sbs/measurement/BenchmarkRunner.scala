/*
 * BenchmarkRunner
 * 
 * Version
 * 
 * Created on September 11th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package measurement

import java.lang.Thread.sleep
import scala.compat.Platform
import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.measurement.BenchmarkType.BenchmarkType

object BenchmarkRunner {

  /**
   * Warms the benchmark up if necessary and measures the desired metric.
   *
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   *
   * @return	The result if success, otherwies a `String` describes the reason.
   */
  def run(metric: BenchmarkType): Either[BenchmarkResult, String] = {

    if (metric == BenchmarkType.MEMORY) {
      log.info("[Benchmarking memory consumption]")
      val runtime: Runtime = Runtime.getRuntime
      run(
        metric,
        series => series forall (_.==(series.head)),
        {
          val start = runtime.freeMemory
          benchmark.init()
          benchmark.run()
          start - runtime.freeMemory
        }
      )
    } else if (metric == BenchmarkType.STARTUP) {
      log.info("[Benchmarking startup state]")
      if (benchmark.initCommand()) {
        run(
          metric,
          _ => true,
          {
            val start = Platform.currentTime
            benchmark.runCommand()
            Platform.currentTime - start
          }
        )
      } else {
        Right("Benchmark process failed.")
      }
    } else {
      log.info("[Benchmarking steady state]")
      benchmark.init()
      run(
        metric,
        series => (Statistic CoV series) < Constant.STEADY_THREDSHOLD,
        {
          val start = Platform.currentTime
          (1 to config.runs) map (_ => benchmark.run)
          Platform.currentTime - start
        }
      )
    }
  }

  def run(metric: BenchmarkType,
          checkWarm: BenchmarkResult => Boolean,
          measure: => Long): Either[BenchmarkResult, String] = {

    log.verbose("")
    log.verbose("--Warmup--")

    var result = new BenchmarkResult(metric)

    val iteratorMax = config.multiplier * 5
    var iteratorCount = 0
    var iteratorMeasure = 0

    while (iteratorMeasure < Constant.MAX_MEASUREMENT && !result.isReliable) {

      result.clear()

      log.verbose("")
      log.verbose("--Start getting a series--")

      for (mul <- 1 to config.multiplier) {
        cleanUp()
        result += measure
        log.verbose("----Measured----  " + result.last)
      }
      iteratorCount = config.multiplier

      while (iteratorCount < iteratorMax && !checkWarm(result)) {
        log.verbose("----Measured----  " + result.last)

        cleanUp()
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