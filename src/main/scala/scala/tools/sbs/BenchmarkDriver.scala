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

import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.util.Report
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.measurement.Benchmark
import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurerFactory


/**
 * Object controls the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {

  /**
   * Start point of the benchmark driver
   * Does the following:
   * <ul>
   * <li>Parse input parameters
   * <li>Compile the sources of the benchmarks if necessary
   * <li>Run all the benchmarks with the specified parameters
   * <li>Run comparisons to previous results
   * <li>Stores the benchmark result into file
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    val (config, log, benchmark) = ArgumentParser.parse(args)

    log.debug(config.toString())

    try {
      if (config.compile && !benchmark.compile()) {
        System.exit(1)
      }

      log.verbose("[Measure]")

      if (config.sampleNumber > 0) {
        benchmark.metrics foreach (
          new Persistor(log, config, benchmark, config.persistorLocation) generate (_, config.sampleNumber))
      }

      val report = new Report(log, benchmark)

      benchmark.metrics foreach (
        metric => {
          val measurer = new MeasurerFactory(log, config) create metric
          measurer run benchmark match {
          case success: MeasurementSuccess => {
            val passOrFail = detectRegression(log, config, ret)
            success.store(passOrFail) match {
              case Some(f) => log.info("Result stored into " + f.path)
              case None => log.info("Cannot stored the result.")
            }
          }
          case failure: MeasurementFailure => report(Constant.REGRESSION_FAILED, Report dueToReason s)
        }
        }
      )
    } catch {
      case e: Exception => {
        val report = new Report
        report(Constant.REGRESSION_FAILED, Report dueToException e)
      }
    }
  }

  /**
   * Loads previous results and uses statistically rigorous method to detect regression.
   *
   * @param result	The benchmark result just measured.
   */
  def detectRegression(log: Log, config: Config, result: MeasurementResult): Boolean = {
    val persistor = new Persistor(log, config, result.benchmark, (config.persistorLocation / result.metric.toString).toDirectory)
    val report = new Report

    persistor add result
    persistor load result.metric

    if (persistor.length < 2) {
      report(Constant.REGRESSION_FAILED,
        Report dueToReason "Not enough result files specified at " + persistor.location.path)
      true
    } else {
      new Statistic(log, config, 0) testDifference persistor match {
        case Left(c) => c match {
          case None => {
            report(Constant.REGRESSION_PASS, null)
            true
          }
          case Some((left, right)) => {
            report(Constant.REGRESSION_FAILED, Report dueToCITest (left, right))
            false
          }
        }
        case Right(r) => r match {
          case None => {
            report(Constant.REGRESSION_PASS, null)
            true
          }
          case Some(meanArray) => {
            report(Constant.REGRESSION_FAILED, Report dueToFTest meanArray)
            false
          }
        }
      }
    }
  }

}
