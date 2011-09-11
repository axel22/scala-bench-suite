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
      if (config.compile) {
        if (!benchmark.compile()) {
          System.exit(1)
        }
      }
      if (config.sampleNumber > 0) {
        Persistor.generate(config.sampleNumber)
      }

      log.verbose("[Measure]")

      BenchmarkRunner.run() match {
        case Left(ret) => println(ret.toString())
        case Right(s) => println(s)
      }

    } catch {
      case e: Exception => {
        throw e
        //        val report = new Report
        //        report(log, config, Constant.FAILED, Report dueToException e)
      }
    }
  }

}
