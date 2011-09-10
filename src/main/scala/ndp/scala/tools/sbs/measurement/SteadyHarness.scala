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
import java.net.URLClassLoader
import java.net.URL

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
object SteadyHarness extends SubProcessHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
   * <li>Iterates the invoking of benchmark <code>main</code> method for it to reach the steady state.
   * <li>Measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  def run(): Either[BenchmarkResult, String] = {
    log("[Benchmarking steady state]")

    val steadyThreshold: Double = 0.02
    //    val clazz = Class forName config.classname
//    val clazz = (new URLClassLoader(Array(new URL("file:" + benchmark.buildPath.path + "/")))).loadClass(benchmark.name)
//    val benchmarkMainMethod = clazz.getMethod("main", classOf[Array[String]])

    benchmark.init()
    runBenchmark(
      (result: BenchmarkResult) => (Statistic CoV result) < steadyThreshold,
      {
        val start = Platform.currentTime
        for (i <- 0 to config.runs) {
//          benchmarkMainMethod.invoke(clazz, { null })
          benchmark.run()
        }
        val end = Platform.currentTime
        end - start
      }
    )
  }

}
