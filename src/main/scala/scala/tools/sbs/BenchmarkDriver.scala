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

import java.lang.System

import scala.tools.sbs.io.Log
import scala.tools.sbs.io.ReportFactory
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.ExceptionFailure
import scala.tools.sbs.regression.History
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.regression.StatisticsFactory

/** Object controls the runtime of benchmark classes to do measurements.
 *
 *  @author ND P
 */
object BenchmarkDriver {

  /** Start point of the benchmark driver
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

    log.debug(config.toString())

    try {
      val compiler = BenchmarkCompilerFactory(log, config)
      val compiled = benchmarks filterNot (benchmark => benchmark.shouldCompile && !(compiler compile benchmark))
      benchmarks filterNot (compiled contains _) foreach (
        // TODO report failure 
        _ => ())

      log.verbose("[Measure]")

      config.modes foreach (mode => {

        val measurer = MeasurerFactory(log, config, mode)

        compiled foreach (benchmark => try {

          val persistor = PersistorFactory(log, config, benchmark, mode)

          measurer measure benchmark match {
            case success: MeasurementSuccess => {
              val result = detectRegression(log, benchmark, success, persistor)
              val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
              report(result)
              persistor.store(success, result)
            }
            case failure: MeasurementFailure => {
              val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
              report(new ImmeasurableFailure(failure))
            }
          }
        } catch {
          case e: Exception => {
            val persistor = PersistorFactory(log, config, benchmark, mode)
            val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
            report(new ExceptionFailure(e))
          }
        })
      })
    } catch {
      // TODO
      case e: Exception => throw e
    }
  }

  /** Loads previous results and uses statistically rigorous method to detect regression.
   *
   *  @param result	The benchmark result just measured.
   */
  def detectRegression(log: Log, benchmark: Benchmark, result: MeasurementSuccess, persistor: Persistor): BenchmarkResult = {

    val history: History =
      if (benchmark.sampleNumber > 0) persistor generate benchmark.sampleNumber
      else persistor.load()
    history add result.series

    if (history.length < 2) {
      NoPreviousFailure(result)
    } else {
      val statistic = StatisticsFactory(log)
      statistic testDifference (result, history)
    }
  }

}
