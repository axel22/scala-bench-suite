/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.sbs.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.regression.History
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.regression.StatisticsFactory

/** Object controls the runtime of benchmark classes to do measurements.
 *
 *  @author ND P
 */
object BenchmarkDriver {

  /** Start point of the benchmark driver.
   *  Does the following:
   *  <ul>
   *  <li>Parse input parameters
   *  <li>Compile the sources of the benchmarks if necessary
   *  <li>Run all the benchmarks with the specified parameters
   *  <li>Run comparisons to previous results
   *  <li>Stores the benchmark result into file
   *  </ul>
   */
  def main(args: Array[String]): Unit = {

    val (config, log, benchmarks) = ArgumentParser.parse(args)

    log.debug(config.toString)

    try {
      if (config.isCleanup) {
        // TODO cleanup
      }
      var resultPack = new ResultPack()
      val compiler = BenchmarkCompilerFactory(log, config)
      val compiled = benchmarks filterNot (benchmark => benchmark.shouldCompile && !(compiler compile benchmark))
      benchmarks filterNot (compiled contains _) foreach (resultPack add CompileFailure(_))

      log.verbose("[Measure]")

      config.modes foreach (mode => {

        val measurer = MeasurerFactory(config, mode)

        compiled foreach (benchmark => try measurer measure benchmark match {
          case success: MeasurementSuccess => {
            val persistor = PersistorFactory(log, config, benchmark, mode)
            val result = detectRegression(benchmark, mode, success, persistor, log)
            resultPack add result
            persistor.store(success, result)
          }
          case failure: MeasurementFailure => {
            resultPack add ImmeasurableFailure(benchmark, failure)
          }
        } catch { case e: Exception => resultPack add ExceptionFailure(benchmark, e) })
      })
      // TODO report here
      resultPack foreach println
    } catch { case e: Exception => throw e }
  }

  /** Loads previous results and uses statistically rigorous method to detect regression.
   *
   *  @param result	The benchmark result just measured.
   */
  def detectRegression(benchmark: Benchmark,
                       mode: BenchmarkMode,
                       result: MeasurementSuccess,
                       persistor: Persistor,
                       log: Log): BenchmarkResult = {

    val history: History =
      if (benchmark.sampleNumber > 0) persistor generate benchmark.sampleNumber
      else persistor.load()
    history add result.series

    if (history.length < 2) {
      NoPreviousFailure(benchmark, result)
    } else {
      val statistic = StatisticsFactory(log)
      statistic testDifference (benchmark, mode, result, history)
    }
  }

}
