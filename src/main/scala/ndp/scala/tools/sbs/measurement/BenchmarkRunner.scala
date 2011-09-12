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
import ndp.scala.tools.sbs.util.BenchmarkType
import ndp.scala.tools.sbs.util.Constant

object BenchmarkRunner extends SubProcessRunner {

  /**
   * Warms the benchmark up if necessary and measures the desired metric.
   *
   * @param	checkWarm	The function checking whether the benchmark has reached steady state
   * @param measure	The thunk to calculate the desired metric
   * 
   * @return	The result if success, otherwies a `String` describes the reason.
   */
  def run(): Either[BenchmarkResult, String] = {

    if (config.benchmarkType == BenchmarkType.MEMORY) {

      log("[Benchmarking memory consumption]")

      val runtime: Runtime = Runtime.getRuntime
      run(
        series => (series map (t => t == series.head) filter (b => b)).length == series.length,
        {
          val start = runtime.freeMemory
          benchmark.init()
          benchmark.run()
          start - runtime.freeMemory
        }
      )
    } else if (config.benchmarkType == BenchmarkType.STARTUP) {
     
      log("[Benchmarking startup state]")

      val processBuilder = new ProcessBuilder(
        config.JAVACMD,
        "-cp",
        config.SCALALIB,
        config.JAVAPROP,
        "scala.tools.nsc.MainGenericRunner",
        "-classpath",
        benchmark.buildPath.path +
          (System getProperty "path.separator") +
          config.classpath,
        benchmark.name
      )

      log.debug(processBuilder.command.toString)

      // Ignore the first launch due to system status changing
      processBuilder.start.waitFor

      run(
        _ => true,
        {
          val start = Platform.currentTime
          processBuilder.start.waitFor
          Platform.currentTime - start
        }
      )
    } else {
      log("[Benchmarking steady state]")

      benchmark.init()
      run(
        series => (Statistic CoV series) < Constant.STEADY_THREDSHOLD,
        {
          val start = Platform.currentTime
          (1 to config.runs) map (_ => benchmark.run)
          Platform.currentTime - start
        }
      )
    }
  }

  def run(checkWarm: BenchmarkResult => Boolean, measure: => Long): Either[BenchmarkResult, String] = {

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
        cleanUp()
        result += measure

        log.verbose("----Measured----  " + result.last)
      }
      iteratorCount = config.multiplier

      while (iteratorCount < iteratorMax && !checkWarm(result)) {
        cleanUp()

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