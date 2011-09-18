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

import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.regression.ANOVAFailure
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.BenchmarkSuccess
import scala.tools.sbs.regression.ConfidenceIntervalFailure
import scala.tools.sbs.regression.ExceptionFailure
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.SimpleFilePersistor
import scala.tools.sbs.regression.StatisticFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.util.ReportFactory

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

    val (config, log, benchmark, persistor) = ArgumentParser.parse(args)

    log.debug(config.toString())

    try {
      if (config.compile && !benchmark.compile()) {
        System.exit(1)
      }

      log.verbose("[Measure]")

      if (config.sampleNumber > 0) {
        benchmark.modes foreach (persistor generate (_, config.sampleNumber))
      }

      val report = new ReportFactory() create ()

      benchmark.modes foreach (
        mode => {
          val measurer = new MeasurerFactory(log, config) create mode
          measurer run benchmark match {
            case success: MeasurementSuccess => {

              detectRegression(log, config, success, benchmark, persistor, mode) match {
                case ok: BenchmarkSuccess => report(ok)
                case ci: ConfidenceIntervalFailure => report(ci)
                case anova: ANOVAFailure => report(anova)
              }

              val storerFactory = new LoadStoreManagerFactory(log, config, benchmark, mode)
              // TODO: passed or failed
              if (persistor.isInstanceOf[SimpleFilePersistor]) {
                val storer = storerFactory create (persistor.asInstanceOf[SimpleFilePersistor].location)
                if (storer.storeMeasurementResult(success)) {
                  log.info("Result stored OK")
                } else {
                  log.info("Cannot storing failed.")
                }
              }
            }
            case failure: MeasurementFailure => report(new ImmeasurableFailure(benchmark))
          }
        }
      )
    } catch {
      case e: Exception => {
        val report = new ReportFactory() create ()
        report(new ExceptionFailure(benchmark, e))
      }
    }
  }

  /**
   * Loads previous results and uses statistically rigorous method to detect regression.
   *
   * @param result	The benchmark result just measured.
   */
  def detectRegression(log: Log,
                       config: Config,
                       result: MeasurementResult,
                       benchmark: Benchmark,
                       persistor: Persistor,
                       mode: BenchmarkMode): BenchmarkResult = {

    persistor add result.series

    val storerFactory = new LoadStoreManagerFactory(log, config, benchmark, mode)
    if (persistor.isInstanceOf[SimpleFilePersistor]) {
      val storer = storerFactory create (persistor.asInstanceOf[SimpleFilePersistor].location)
      storer.loadPersistor()
    } else {
      // Another kind of LoadStoreManager
    }

    if (persistor.length < 2) {
      NoPreviousFailure(benchmark, persistor)
    } else {
      val statistic = new StatisticFactory(log, config) create ()
      statistic testDifference (benchmark, persistor)
    }
  }

}
