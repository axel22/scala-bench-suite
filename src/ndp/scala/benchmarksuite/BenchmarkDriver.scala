/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on May 25th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite

import scala.tools.nsc.Global
import scala.tools.nsc.Settings

import ndp.scala.benchmarksuite.measurement.Harness
import ndp.scala.benchmarksuite.measurement.MemoryHarness
import ndp.scala.benchmarksuite.measurement.StartupHarness
import ndp.scala.benchmarksuite.measurement.SteadyHarness
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Constant
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel
import ndp.scala.benchmarksuite.utility.Report

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
   * <li>Load the previous results
   * <li>Run comparisons to previous results
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    val log = new Log
    val config: Config = ArgumentParser parse args

    if (config.LOG_LEVEL == LogLevel.DEBUG) {
      log debug config.toString
    }

    try {
      if (config.COMPILE) {
        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
          log verbose "[Compile]"
        }

        val settings = new Settings(log.error)
        val (ok, errArgs) = settings.processArguments(List("-d", config.BENCHMARK_BUILD.path, config.SRCPATH.path), true)
        if (ok) {
          val compiler = new Global(settings)
          (new compiler.Run) compile List(config.SRCPATH.path)
        } else {
          errArgs map (err => log error err)
          System exit 1
        }
      }

      if (config.LOG_LEVEL == LogLevel.VERBOSE) {
        log verbose "[Measure]"
      }

      var harness: Harness = null

      if (config.BENCHMARK_TYPE == BenchmarkType.MEMORY) {
        harness = new MemoryHarness(log, config)
      } else if (config.BENCHMARK_TYPE == BenchmarkType.STARTUP) {
        harness = new StartupHarness(log, config)
      } else {
        harness = new SteadyHarness(log, config)
      }
      harness.run

    } catch {
      /*case e: java.lang.reflect.InvocationTargetException => e.getCause match {
        case n: java.lang.ClassNotFoundException => log error "Class " + n.getMessage() + " not found."
        case n: java.lang.NoClassDefFoundError => log error "Class " + n.getMessage() + " not found."
        case n => report(log, config, Constant.FAILED, Report dueToException n)
      }
      case e: java.lang.ClassNotFoundException => log debug "Class " + e.getMessage + " not found."*/
      case f: Exception => {
        val report = new Report
        report(log, config, Constant.FAILED, Report dueToException f)
      }
    }
  }

}
