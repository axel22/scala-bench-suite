/*
 * SteadyHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.regression.BenchmarkResult
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.regression.Persistor

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
class SteadyHarness(log: Log, config: Config) extends Harness(log, config) {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
   * <li>Iterates the invoking of benchmark <code>main</code> method for it to reach the steady state.
   * <li>Iterates the invoking of benchmark <code>main</code> method in its steady state to measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  override def run(): BenchmarkResult = {

    log("[Benchmarking steady state]")

    val steadyThreshold: Double = 0.02
    var benchmarkMainMethod: Method = null
    var result: BenchmarkResult = new BenchmarkResult

    var start: Long = 0
    var end: Long = 0
    //    var statistic: Statistic = new Statistic(log, config)

    val args = { null }

    try {
      val clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH)))).loadClass(config.CLASSNAME)
      benchmarkMainMethod = clazz.getMethod("main", classOf[Array[String]])

      log verbose "[Warm Up] "

      for (mul <- 1 to config.MULTIPLIER) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to config.RUNS) {
          benchmarkMainMethod.invoke(clazz, args)
        }
        end = Platform.currentTime

        result += end - start

      }
      log debug "[Standard Deviation] " + (Statistic standardDeviation result) +
        "	[Sample Mean] " + (Statistic mean result).formatted("%.2f") +
        "	[CoV] " + (Statistic CoV result)

      while ((Statistic CoV result) >= steadyThreshold) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to config.RUNS) {
          benchmarkMainMethod.invoke(null, args)
        }
        end = Platform.currentTime

        result remove 0
        result += end - start

        log debug "[Newest] " + result.last

        log debug "[Standard Deviation] " + (Statistic standardDeviation result) +
          "	[Sample Mean] " + (Statistic mean result).formatted("%.2f") + "	[CoV] " +
          (Statistic CoV result)
      }

      log verbose "[Steady State] "

      result = new BenchmarkResult

      for (mul <- 1 to config.MULTIPLIER) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to config.RUNS) {
          benchmarkMainMethod.invoke(null, args)
        }
        end = Platform.currentTime

        result += end - start
      }

      constructStatistic(log, config, result)
      
      log verbose "[End constructing statistical metric]"

      detectRegression(log, config, result)
      (new Persistor(log, config) += result).store
      result
    } catch {
      case e: java.lang.reflect.InvocationTargetException => {
        e.getCause match {
          case n: java.lang.ClassNotFoundException => {
            log("Class " + n.getMessage() + " not found. Please check the class directory.")
            null
          }
          case n: java.lang.NoClassDefFoundError => {
            log("Class " + n.getMessage() + " not found. Please check the class directory.")
            null
          }
          case n => throw n
        }
      }
      case i => throw i
    }
  }

}
