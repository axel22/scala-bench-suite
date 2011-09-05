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

import java.io.BufferedReader
import java.io.InputStreamReader

import scala.tools.nsc.Global
import scala.tools.nsc.Settings

import ndp.scala.tools.sbs.measurement.SteadyHarness
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.LogLevel

/**
 * Object controls the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {

  private var _log: Log = null
  def log = _log
  def log_=(log: Log) = _log = log

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

    val config: Config = ArgumentParser parse args
    log = new Log(config)

    if (config.LOG_LEVEL == LogLevel.DEBUG) {
      log debug config.toString
    }

    try {
      if (config.COMPILE) {
        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
          log verbose "[Compile]"
        }

        val settings = new Settings(log.error)
        val (ok, errArgs) = settings.processArguments(
          List(
            "-classpath", config.CLASSPATH,
            "-d", config.BENCHMARK_BUILD.path,
            config.SRCPATH.path),
          true)

        if (ok) {
          println(settings.d)
          val compiler = new Global(settings)
          //          (new compiler.Run) compile List(config.SRCPATH.path)
        } else {
          errArgs map (err => log error err)
          System exit 1
        }
      }

      log verbose "[Measure]"

      val processBuilder = new ProcessBuilder(
        config.JAVACMD,
        "-cp",
        config.SCALA_LIB,
        config.JAVAPROP,
        "scala.tools.nsc.MainGenericRunner",
        "-classpath",
        SteadyHarness.getClass.getProtectionDomain.getCodeSource.getLocation.getPath + ";" + config.BENCHMARK_BUILD.path + ";" + "d:/university/5thyear/internship/working/nsbs/lib/commons-math-2.2.jar",
        SteadyHarness.getClass.getName replace ("$", ""),
        config toArgument
      )

      println(processBuilder.command)

      val process = processBuilder.start
      val stdout = process.getInputStream
      val stderr = process.getErrorStream

      val readerOut = new BufferedReader(new InputStreamReader(stdout))
      var line: String = readerOut.readLine
      while (line != null) {
        println("Stdout: " + line)
        line = readerOut.readLine
      }
      readerOut.close

      val readerErr = new BufferedReader(new InputStreamReader(stderr))
      line = readerErr.readLine
      while (line != null) {
        println("Stderr: " + line)
        line = readerErr.readLine
      }
      readerErr.close
    } catch {
      /*case e: java.lang.reflect.InvocationTargetException => {
        e.getCause match {
        case n: java.lang.ClassNotFoundException => {
          log error "Class " + n.getMessage() + " not found."
        }
        case n: java.lang.NoClassDefFoundError => log error "Class " + n.getMessage() + " not found."
        case n => log error n.toString
        }
      }
      case e: java.lang.ClassNotFoundException => {
        log debug "Class " + e.getMessage + " not found."
      }
      case f: Exception => {
        val report = new Report
        report(log, config, Constant.FAILED, Report dueToException f)
      }*/
      case e: Exception => {
        throw e
        //        val report = new Report
        //        report(log, config, Constant.FAILED, Report dueToException e)
      }
    }
  }

}
