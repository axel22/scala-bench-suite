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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.common.BenchmarkCompilerFactory
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.ReportFactory
import scala.tools.sbs.io.UI
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.profiling.ProfilingFailure
import scala.tools.sbs.profiling.ProfilingSuccess
import scala.tools.sbs.regression.History
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.regression.RegressionResult
import scala.tools.sbs.regression.RegressionSuccess
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.util.FileUtil

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
  def main(args: Array[String]): Unit = try {

    UI.info("Parsing arguments")
    val (config, log, benchmarkInfos) = ArgumentParser parse args

    log.debug(config.toString)

    // Clean up in case demanded
    if (config.isCleanup) {
      UI.info("Cleaning up")
      for (mode <- config.modes) {
        FileUtil.clean(config.history / mode.location)
      }
    }

    var resultPack = new ResultPack()

    UI.info("Compiling benchmarks")
    val compiler = BenchmarkCompilerFactory(log, config)
    val compiled = benchmarkInfos map (_.expand(compiler, config)) filterNot (_ == null)

    log.debug(compiled.toString)

    // Add failure compiles for reporting
    benchmarkInfos filterNot (info => compiled exists (_.name == info.name)) foreach (resultPack add CompileFailure(_))

    try {
      UI.info("Generating sample history")
      // Generate sample history in case demanded
      compiled filter (_.sampleNumber > 0) foreach (toGenerated =>
        config.modes.filterNot(_ equals Profiling) foreach (
          PersistorFactory(log, config, toGenerated, _) generate toGenerated.sampleNumber))
    } catch {
      case e => log.debug(e.toString())
    }

    // List of benchmarks to be run and detect regression
    val toRun = compiled filter (_.sampleNumber == 0)
    log.debug(toRun.toString)

    log.verbose("--Running--")
    UI.info("Running")

    config.modes foreach (mode => {

      UI.info("Mode: " + mode)

      FileUtil.mkDir(config.benchmarkDirectory / mode.location) match {
        case Right(s) => log.error(s)
        case _ => ()
      }

      val runner = RunnerFactory(log, config, mode)

      toRun foreach (benchmark => {

        UI.info("Benchmark: " + benchmark.name)
        log.info("Benchmark: " + benchmark.name)

        try runner run benchmark match {
          case success: RunSuccess => {

            UI.info("[  Run OK  ]")
            log.verbose("[  Run OK  ]")

            val persistor = PersistorFactory(log, config, benchmark, mode)
            success match {
              case msm: MeasurementSuccess => {

                UI.info("Detect regression")
                val result = detectRegression(benchmark, mode, msm, persistor, log)

                result match {
                  case _: RegressionSuccess => {
                    UI.info("[  OK  ]")
                    persistor.store(msm, true)
                  }
                  case _: NoPreviousFailure => {
                    UI.info("[FAILED]")
                    persistor.store(msm, true)
                  }
                  case _ => {
                    UI.info("[FAILED]")
                    persistor.store(msm, false)
                  }
                }
                resultPack add result
              }
              case pfl: ProfilingSuccess => {
                pfl.profile.classes foreach (clazz => {
                  clazz.methodInvoked foreach (method => {
                    print("    " + method.name + " invoked:")
                    method.invocations foreach (invo => {
                      print(" " + invo.time)
                    })
                    println
                  })
                })
                resultPack add pfl
                persistor.store(pfl, true)
              }
            }
          }
          case failure: RunFailure => {

            UI.info("[Run FAILED]")
            log.verbose("[Run FAILED]")

            failure match {
              case msm: MeasurementFailure => {
                resultPack add ImmeasurableFailure(benchmark, mode, msm)
              }
              case pfl: ProfilingFailure => {
                resultPack add pfl
              }
            }
          }
        } catch { case e: Exception => resultPack add ExceptionFailure(benchmark, mode, e) }
      })
      FileUtil.cleanLog(config.benchmarkDirectory / mode.location)

    })
    ReportFactory(config)(resultPack)
  } catch {
    case e: Throwable => {
      UI.info(e.toString)
      UI.info(e.getStackTraceString)
      throw e
    }
  }

  /** Loads previous results and uses statistically rigorous method to detect regression.
   *
   *  @param result	The benchmark result just measured.
   */
  def detectRegression(benchmark: Benchmark,
                       mode: BenchmarkMode,
                       result: MeasurementSuccess,
                       persistor: Persistor,
                       log: Log): RegressionResult = {

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
