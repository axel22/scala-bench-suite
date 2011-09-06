/*
 * SteadyHarness
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package measurement

import scala.compat.Platform

import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
object SteadyHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
   * <li>Iterates the invoking of benchmark <code>main</code> method for it to reach the steady state.
   * <li>Measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    try {

      val confArgs = args(0) split " "
      val logArgs = args(1) split " "
      
      for (c <- confArgs) {
        println(c)
      }
      for (l <- logArgs) {
        println(l)
      }
      
      config = new Config(confArgs)

      log = new Log(logArgs)

      log("[Benchmarking steady state]")

      val steadyThreshold: Double = 0.02
      val clazz = Class forName config.classname
      val benchmarkMainMethod = clazz.getMethod("main", classOf[Array[String]])

      val result = runBenchmark(
        (result: BenchmarkResult) => (Statistic CoV result) < steadyThreshold,
        {
          val start = Platform.currentTime
          for (i <- 0 to config.runs) {
            benchmarkMainMethod.invoke(clazz, { null })
          }
          val end = Platform.currentTime
          end - start
        }
      )

      for (ret <- result) {
        Console println ret
      }

      System exit 0
    } catch {
      case e =>
        println(e.toString)
        println(e.getStackTraceString)
        System exit 1
    }
  }

}
