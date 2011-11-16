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
import java.text.SimpleDateFormat
import java.util.Date

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
    benchmarkInfos filterNot (compiled contains _) foreach (_ foreach (failure => {
      val compileFailed = CompileBenchmarkFailure(failure)
      notify(compileFailed, null)
      resultPack add compileFailed
    }))

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

      val benchmarks = compiled(mode) map (info =>
        try info.expand(runner.benchmarkFactory, config)
        catch {
          case e @ (_: ClassNotFoundException | _: ClassCastException) => {
            UI.error(e.toString)
            log.error(e.toString)
            null
          }
        }) filterNot (_ == null)

      UI.info("[Expanding completed]")

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

        try {
          val result = runner run benchmark
          notify(result, mode)
          resultPack add result
        }
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
    overallReport(config, resultPack)
    ReportFactory(config)(resultPack)
  }
  catch {
    case e: Throwable => {
      UI.error(e.toString)
      UI.error(e.getStackTraceString)
      throw e
    }
  }

  def notify(each: BenchmarkResult, mode: BenchmarkMode) = {
    val last = each match {
      case _: BenchmarkSuccess => "[  OK  ]"
      case _                   => "[FAILED]"
    }
    val modename = if (mode == null) "compile" else mode.location
    System.out.format("%-10s | %-20s %10s\n", modename, each.benchmarkName, last)
  }

  def overallReport(config: Config, pack: ResultPack) {
    println("========================================================================")
    println("Benchmarking date:   " + new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date))
    println("Directory:           " + config.benchmarkDirectory.path)
    println("Total benchmarks:    " + pack.total)
    println("OK:                  " + pack.ok)
    println("Failed:              " + pack.failed)
    println("========================================================================")
  }

}
