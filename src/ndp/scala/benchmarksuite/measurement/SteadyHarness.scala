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
import scala.compat.Platform
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.BenchmarkResult
import scala.collection.mutable.ArrayBuffer

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
class SteadyHarness(CLASSNAME: String, CLASSPATH: String, RUNS: Int, MULTIPLIER: Int) extends Harness {

  private var benchmarkMainMethod: Method = null
  /**
   * The thredshold used to determine whether the given <code>main</code> method has reached the steady state.
   */
  private val steadyThreshold: Double = 0.02

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

    var benchmarkMainMethod: Method = null
    var start: Long = 0
    var end: Long = 0
    var statistic: Statistic = new Statistic(new ArrayBuffer)

    val args = { null }

    try {
      val clazz = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
      benchmarkMainMethod = clazz.getMethod("main", classOf[Array[String]])

      log.debug("[Warm Up] ")

      for (mul <- 1 to MULTIPLIER) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to RUNS) {
          benchmarkMainMethod.invoke(clazz, args)
        }
        end = Platform.currentTime

        statistic.series += end - start

      }
      log.verbose("[Standard Deviation] " + statistic.standardDeviation + "	[Sample Mean] " + statistic.mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);

      while (statistic.CoV >= steadyThreshold) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to RUNS) {
          benchmarkMainMethod.invoke(null, args)
        }
        end = Platform.currentTime

        statistic.series.remove(0)
        statistic.series += end - start

        log.debug("[Newest] " + statistic.series.last)

        log.verbose("[Standard Deviation] " + statistic.standardDeviation + "	[Sample Mean] " + statistic.mean.formatted("%.2f") + "	[CoV] " + statistic.CoV)
      }

      log.debug("[Steady State] ")

      statistic.series = new ArrayBuffer

      for (mul <- 1 to MULTIPLIER) {

        cleanUp

        start = Platform.currentTime
        for (i <- 0 to RUNS) {
          benchmarkMainMethod.invoke(null, args)
        }
        end = Platform.currentTime

        statistic.series += end - start
      }

      constructStatistic(statistic.series)

      result = new BenchmarkResult(statistic.series, CLASSNAME, true)
      result.storeByDefault
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
