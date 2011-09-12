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
   * <li>Load the previous results
   * <li>Run comparisons to previous results
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
      if (config.sampleNumber > 0) {
        Persistor.generate(config.sampleNumber)
      }

      log.verbose("[Measure]")

      val report = new Report

      BenchmarkRunner.run() match {
        case Left(ret) => {
          val persistor = new Persistor
          val report = new Report

          persistor += ret
          persistor.load

          Statistic testDifference persistor match {
            case Left(c) => c match {
              case None => report(Constant.REGRESSION_PASS, null)
              case Some((left, right)) => report(Constant.REGRESSION_FAILED, Report dueToCITest (left, right))
            }
            case Right(r) => r match {
              case None => report(Constant.REGRESSION_PASS, null)
              case Some(meanArray) => report(Constant.REGRESSION_FAILED, Report dueToFTest meanArray)
            }
          }
        }
        case Right(s) => {
          report(Constant.REGRESSION_FAILED, Report dueToReason s)
        }
      }
    } catch {
      case e: Exception => {
        val report = new Report
        report(Constant.REGRESSION_FAILED, Report dueToException e)
      }
    }
  }

}
