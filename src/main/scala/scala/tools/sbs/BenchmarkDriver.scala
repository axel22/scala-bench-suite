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

import scala.tools.nsc.io.Path.string2path
import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.common.BenchmarkCompilerFactory
import scala.tools.sbs.common.BenchmarkMode
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.ReportFactory
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.CompileFailure
import scala.tools.sbs.regression.ExceptionFailure
import scala.tools.sbs.regression.History
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.util.FileUtil
import scala.tools.nsc.util.ScalaClassLoader

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
      // Clean up in case demanded
      if (config.isCleanup) {
        for (mode <- config.modes) {
          FileUtil.clean(config.history / mode.location)
        }
      }
      var resultPack = new ResultPack()
      val compiler = BenchmarkCompilerFactory(log, config)
      val compiled = benchmarks filterNot (benchmark => benchmark.shouldCompile && !(compiler compile benchmark))
      log.debug(compiled.toString)
      // Add failure compiles for reporting
      benchmarks filterNot (compiled contains _) foreach (resultPack add CompileFailure(_))

      try {
      // Generate sample history in case demanded
      compiled filter (_.sampleNumber > 0) foreach (toGenerated =>
        config.modes foreach (PersistorFactory(log, config, toGenerated, _) generate toGenerated.sampleNumber))
      } catch {
        case e => log.debug(e.toString())
      }

      // List of benchmarks to be run and detect regression
      val toRun = compiled filter (_.sampleNumber == 0)
      log.debug(toRun.toString)

      log.verbose("[Measure]")

      config.modes foreach (mode => {

        FileUtil.mkDir(config.benchmarkDirectory / mode.location) match {
          case Right(s) => log.error(s)
          case _ => ()
        }

        val measurer = MeasurerFactory(config, mode)

        toRun foreach (benchmark => try measurer measure benchmark match {
          case success: MeasurementSuccess => {
            val persistor = PersistorFactory(log, config, benchmark, mode)
            val result = detectRegression(benchmark, mode, success, persistor, log)
            resultPack add result
            persistor.store(success, result)
          }
          case failure: MeasurementFailure => {
            resultPack add ImmeasurableFailure(benchmark, mode, failure)
          }
        } catch { case e: Exception => resultPack add ExceptionFailure(benchmark, mode, e) })
        FileUtil.cleanLog(config.benchmarkDirectory / mode.location)
      })
      ReportFactory(config)(resultPack)
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

    val history: History = persistor.load()
    history add result.series

    if (history.length < 2) {
      NoPreviousFailure(benchmark, mode, result)
    } else {
      val statistic = StatisticsFactory(log)
      statistic testDifference (benchmark, mode, result, history)
    }
  }

}
