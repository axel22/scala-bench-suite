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

import java.io.File
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import ndp.scala.benchmarksuite.measurement.Harness
import ndp.scala.benchmarksuite.measurement.MemoryHarness
import ndp.scala.benchmarksuite.measurement.StartupHarness
import ndp.scala.benchmarksuite.measurement.SteadyHarness
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.Report
import ndp.scala.benchmarksuite.utility.Constant

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
   * <li>Load the previous results
   * <li>Run comparisons to previous results
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    val log = new Log
    val config: Config = parse(log, args)

    log debug config.toString

    try {
      if (config.COMPILE) {
        log verbose "[Compile]"
        val settings = new Settings(log.error)
        val (ok, errArgs) = settings.processArguments(List("-d", config.BENCHMARK_DIR, config.SRC), true)
        if (ok) {
          val compiler = new Global(settings)
          (new compiler.Run) compile List(config.SRC)
        } else {
          printUsage(log)
          System exit 1
        }
      }

      log verbose "[Measure]"

      var harness: Harness = null

      if (config.BENCHMARK_TYPE == BenchmarkType.Memory) {
        harness = new MemoryHarness(log, config)
      } else if (config.BENCHMARK_TYPE == BenchmarkType.Startup) {
        harness = new StartupHarness(log, config)
      } else {
        harness = new SteadyHarness(log, config)
      }
      harness.run

    } catch {
      /*case e: java.lang.reflect.InvocationTargetException => e.getCause match {
        case n: java.lang.ClassNotFoundException => log error "Class " + n.getMessage() + " not found."
        case n: java.lang.NoClassDefFoundError => log error "Class " + n.getMessage() + " not found."
        case n => report(log, config, Constant.FAILED, Report dueToException n)
      }
      case e: java.lang.ClassNotFoundException => log debug "Class " + e.getMessage + " not found."*/
      case f: Exception => {
        val report = new Report
        report(log, config, Constant.FAILED, Report dueToException f)
      }
    }
  }

  def parse(log: Log, args: Array[String]): Config = {
    var multiplier = 0
    var warmup = 0
    var runs = 0
    var classname = ""
    var src = ""
    var srcpath = ""
    var classdir = ""
    val separator = /*System.getProperty("file.separator")*/ "/"
    val extensionSeparator = "."
    var compile = true

    var i = 0
    try {
      while (i < args.length - 1) {
        log debug args(i)
        log debug args(i + 1)
        args(i) match {
          case "-src" => {
            classname = args(i + 1) substring ((args(i + 1) lastIndexOf separator) + 1, args(i + 1) lastIndexOf extensionSeparator)
            src = args(i + 1)
            log debug ("src " + src)
            srcpath = args(i + 1) substring (0, args(i + 1) lastIndexOf separator)
            log debug ("srcpath " + srcpath)
            i += 1
          }
          case "-help" => {
            printUsage(log)
            System exit 0
          }
          case "-warmup" => {
            try {
              warmup = args(i + 1).toInt
              i += 1
            } catch {
              case _ => {
                printUsage(log)
                System exit 0
              }
            }
          }
          case "-runs" => {
            try {
              runs = args(i + 1).toInt
              i += 1
            } catch {
              case _ => {
                printUsage(log)
                System exit 1
              }
            }
          }
          case "-multiplier" => {
            try {
              multiplier = args(i + 1).toInt
              i += 1
            } catch {
              case _ => {
                printUsage(log)
                System exit 1
              }
            }
          }
          case "-classpath" => {
            classdir = args(i + 1)
            i += 1
          }
          case "-noncompile" => {
            compile = false
          }
          case _ => {
            log debug ("Value of argument " + args(i - 1) + ": " + args(i))
          }
        }
        i += 1
      }
    } catch {
      case e: Exception => {
        log debug e.toString
        printUsage(log)
        System exit 1
      }
    }
    if ((src equals "") || (runs == 0)) {
      printUsage(log)
      System exit 1
    }
    if (multiplier == 0) {
      multiplier = 1
    }
    if (classdir equals "") {
      classdir = srcpath /*+ separator + "build"*/
    }
    log debug "[Arguments] " + classname + " " + classdir + " " + warmup + " " + runs + " " + multiplier + " " + compile

    new File(srcpath) mkdir

    new Config(src, classname, classdir, separator, runs, multiplier, "output/Memory", BenchmarkType.Memory, true)
  }

  def printUsage(log: Log) {
    log yell "Usage: BenchmarkSuite -src <scala source file> -warmup <warm up> -runs <runs> [-multiplier <multiplier>] [-noncompile] [-classdir <classdir>] [-help]"
    log yell "	The benchmark is warmed up <warm up> times, then run <runs> times, forcing a garbage collection between runs."
    log yell "	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions."
    log yell "	The optional -noncompile causes the benchmark not to be recompiled."
    log yell "	The optional -classdir causes the generated class files to be placed at <classdir>"
    log yell "	The optional -help prints this usage."
  }

}
