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

import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.ExceptionFailure
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.regression.StatisticFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.util.ReportFactory
import scala.sys.process.ProcessIO
import scala.tools.sbs.measurement.MemoryHarness
import scala.collection.mutable.ArrayBuffer

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
      if (config.compile) {
        if (new BenchmarkCompilerFactory(log, config).create() compile benchmark) ()
        else System.exit(1)
      }

      log.verbose("[Measure]")

      if (config.sampleNumber > 0) {
        config.modes foreach (mode => {
          val persistor = new PersistorFactory(log, config).create(benchmark, mode)
          persistor generate config.sampleNumber
        })
      }

      config.modes foreach (mode => try {

        val measurer = new MeasurerFactory(log, config) create mode

        benchmarks foreach (benchmark => {

          // TODO: invoke new JVM

          rebuildResult(subProcessOutput) match {
            case success: MeasurementSuccess => {
              val persistor = new PersistorFactory(log, config).create(benchmark, mode)
              val result = detectRegression(log, config, success, persistor)
              val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
              report(result)
              persistor.store(success, result)
            }
            case failure: MeasurementFailure => {
              val persistor = new PersistorFactory(log, config).create(benchmark, mode)
              val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
              report(new ImmeasurableFailure(failure))
            }
          }
        })
      } catch {
        case e: Exception => {
          val persistor = new PersistorFactory(log, config).create(benchmark, mode)
          val report = new ReportFactory(log, config).create(benchmark, persistor, mode)
          report(new ExceptionFailure(e))
        }
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
  def detectRegression(log: Log,
                       config: Config,
                       result: MeasurementSuccess,
                       persistor: Persistor): BenchmarkResult = {

    persistor add result.series

    if (persistor.length == 1) {
      persistor.load()
    }
    if (persistor.length < 2) {
      NoPreviousFailure(result)
    } else {
      val statistic = new StatisticFactory(log, config).create()
      statistic testDifference (result, persistor)
    }
  }

}
