/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.nsc.io.Path.string2path
import scala.tools.sbs.common.BenchmarkCompilerFactory
import scala.tools.sbs.io.ReportFactory
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.FileUtil

/** Object controls the runtime of benchmark classes to do measurements.
 *
 *  @author ND P
 */
object BenchmarkDriver {

  /** Start point of the benchmark driver.
   *  Does the following:
   *  <ul>
   *  <li>Parse input parameters
   *  <li>Compile the sources of the benchmarks if necessary
   *  <li>Run all the benchmarks with the specified parameters
   *  <li>Run comparisons to previous results
   *  <li>Stores the benchmark result into file
   *  </ul>
   */
  def main(args: Array[String]): Unit = try {

    UI.info("[Parsing arguments]")
    val (config, log, benchmarkInfos) = ArgumentParser parse args

    if (config.isHelp) {
      println(config.helpMsg)
      System exit 0
    }
    log.debug(config.toString)

    // Clean up in case demanded
    if (config.isCleanup) {
      UI.info("[Cleaning up]")
      for (mode <- config.modes) {
        FileUtil.clean(config.history / mode.location)
      }
    }

    val resultPack = new ResultPack()

    UI.info("[Compiling benchmarks]")
    val compiler = BenchmarkCompilerFactory(log, config)

    val compiled = benchmarkInfos filter (_.isCompiledOK(compiler, config))

    log.debug(compiled.toString)

    // Add failure compiles for reporting
    benchmarkInfos filterNot (
      info => compiled exists (_.name == info.name)) foreach (
        resultPack add CompileBenchmarkFailure(_))

    UI.info("[Expanding completed]")

    config.modes foreach (mode => {

      UI.info("[Benchmarking mode: " + mode.description + "]")
      log.debug("Mode: " + mode.description)

      resultPack switchMode mode

      FileUtil.mkDir(config.benchmarkDirectory / mode.location) match {
        case Right(s) => log.error(s)
        case _        => ()
      }

      val runner = RunnerFactory(config, log, mode)
      log.debug("Runner: " + runner.getClass.getName)

      val benchmarks = compiled map (info =>
        try info.expand(runner.benchmarkFactory, config)
        catch {
          case e @ (_: ClassNotFoundException | _: ClassCastException) => {
            UI.error(e.toString)
            log.error(e.toString)
            null
          }
        }) filterNot (_ == null)

      UI.info("[Generating sample histories]")
      try benchmarks filter (_.sampleNumber > 0) foreach (runner generate _)
      catch {
        case e => log.debug(e.toString)
      }

      // Benchmarking
      UI.info("[Start benchmarking]")
      benchmarks filter (_.sampleNumber == 0) foreach (benchmark => {

        UI.info("Benchmark: " + benchmark.name)
        log.info("Benchmark: " + benchmark.name)
        log.debug("Benchmark: " + benchmark.getClass.getName)

        try resultPack add (runner run benchmark)
        catch {
          case e: Exception => {
            UI.info("[    Run FAILED    ]")
            log.verbose("[    Run FAILED    ]")

            resultPack add new ExceptionBenchmarkFailure(benchmark.name, e)
          }
        }
      })
      if (!config.isNoCleanLog) {
        FileUtil.cleanLog(config.benchmarkDirectory / mode.location)
      }
    })
    ReportFactory(config)(resultPack)
  }
  catch {
    case e: Throwable => {
      UI.error(e.toString)
      UI.error(e.getStackTraceString)
      throw e
    }
  }

}
