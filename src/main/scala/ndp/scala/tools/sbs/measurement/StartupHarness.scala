/*
 * StartupHarness
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

import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.LogLevel

/**
 * Class represent the harness controls the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class StartupHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Creates the operating system process for the benchmark classes to run.
   * <li>Iterates the invoking of new JVM instance loading the benchmark classes to measure performance.
   * <li>And stores the result running time series to file.
   * </ul>
   */
  def run(): Either[BenchmarkResult, String] = {

    log("[Benchmarking startup state]")

    val processBuilder = new ProcessBuilder(
      config.JAVACMD,
      "-cp",
      config.SCALALIB,
      config.JAVAPROP,
      "scala.tools.nsc.MainGenericRunner",
      "-classpath",
      config.benchmarkBuild.path +
        (System getProperty "path.separator") +
        config.classpath,
      config.classname
    )

    log debug processBuilder.command.toString

    var result: BenchmarkResult = new BenchmarkResult

    // Ignore the first launch due to system status changing
    var process = processBuilder.start
    process.waitFor

    runBenchmark(
      _ => true,
      {
        val start = Platform.currentTime
        process = processBuilder.start
        process.waitFor
        val end = Platform.currentTime
        end - start
      }
    )
  }

}
