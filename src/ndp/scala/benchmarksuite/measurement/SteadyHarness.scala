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

import ndp.scala.benchmarksuite.regression.BenchmarkResult
import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log

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
   * <li>Measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  override def run(): BenchmarkResult = {

    log("[Benchmarking steady state]")

    val steadyThreshold: Double = 0.02
    val args = { null }
    val clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH + config.FILE_SEPARATOR)))).loadClass(config.CLASSNAME)
    val benchmarkMainMethod = clazz.getMethod("main", classOf[Array[String]])

    runBenchmark(
      log,
      config,
      (result: BenchmarkResult) => (Statistic CoV result) < steadyThreshold,
      {
        val start = Platform.currentTime
        for (i <- 0 to config.RUNS) {
          benchmarkMainMethod.invoke(clazz, args)
        }
        val end = Platform.currentTime
        end - start
      }
    )
  }

}
