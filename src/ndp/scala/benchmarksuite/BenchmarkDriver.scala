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

import ndp.scala.benchmarksuite.measurement.Harness
import ndp.scala.benchmarksuite.measurement.MemoryHarness
import ndp.scala.benchmarksuite.measurement.StartupHarness
import ndp.scala.benchmarksuite.measurement.SteadyHarness
import ndp.scala.benchmarksuite.utility.Log

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
   * <li>Load the previous results if necessary
   * <li>Run comparisons to previous results if necessary
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    val log = new Log
    try {
      var harness: Harness = new MemoryHarness(args(0), args(1) + "/", args(2).toInt, args(3).toInt)
      harness.run

//      harness = new StartupHarness(args(0), args(1) + "/", args(2).toInt, args(3).toInt)
//      harness.run

//      harness = new SteadyHarness(args(0), args(1) + "/", args(2).toInt, args(3).toInt)
//      harness.run

    } catch {
      case e: java.lang.ClassNotFoundException => {
        log.debug("Class " + e.getMessage() + " not found. Please re-install the application.")
      }
      case f => throw f
    }
  }

}
