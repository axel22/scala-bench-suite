/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs

import ndp.scala.tools.sbs.measurement.BenchmarkResult
import ndp.scala.tools.sbs.measurement.BenchmarkRunner
import ndp.scala.tools.sbs.regression.Persistor
import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.util.Report

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

    val (c, l, b) = ArgumentParser.parse(args)
    config = c
    log = l
    benchmark = b

    log.debug(config.toString())

    try {
      if (config.compile && !benchmark.compile()) {
        System.exit(1)
      }

      log.verbose("[Measure]")

      if (config.sampleNumber > 0) {
        config.metrics foreach (metric => Persistor.generate(metric, config.sampleNumber))
      }

      val report = new Report

      config.metrics foreach (
        metric => BenchmarkRunner.run(metric) match {
          case Left(ret) => {
            val passOrFail = detectRegression(ret)
            ret.store(passOrFail) match {
              case Some(f) => log.info("Result stored into " + f.path)
              case None => log.info("Cannot stored the result.")
            }
          }
          case Right(s) => report(Constant.REGRESSION_FAILED, Report dueToReason s)
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
  def detectRegression(result: BenchmarkResult): Boolean = {
    val persistor = new Persistor((config.persistorLocation / result.metric.toString).toDirectory)
    val report = new Report

    persistor += result
    persistor.load(result.metric)
    
    persistor foreach println

    if (persistor.length < 2) {
      report(Constant.REGRESSION_FAILED,
        Report dueToReason "Not enough result files specified at " + persistor.location.path)
        true
    } else {
      Statistic testDifference persistor match {
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
