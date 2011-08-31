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

import java.io.{ File => JFile }
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import ndp.scala.benchmarksuite.measurement.Harness
import ndp.scala.benchmarksuite.measurement.MemoryHarness
import ndp.scala.benchmarksuite.measurement.StartupHarness
import ndp.scala.benchmarksuite.measurement.SteadyHarness
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Constant
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel
import ndp.scala.benchmarksuite.utility.Report
import scala.tools.nsc.io.Path

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

    if (config.LOG_LEVEL == LogLevel.DEBUG) {
      log debug config.toString
    }

    try {
      if (config.COMPILE) {
        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
          log verbose "[Compile]"
        }

        val settings = new Settings(log.error)
        val (ok, errArgs) = settings.processArguments(List("-d", config.BENCHMARK_BUILD.path, config.SRC.path), true)
        if (ok) {
          val compiler = new Global(settings)
          (new compiler.Run) compile List(config.SRC.path)
        } else {
          printUsage(log)
          System exit 1
        }
      }

      if (config.LOG_LEVEL == LogLevel.VERBOSE) {
        log verbose "[Measure]"
      }

      var harness: Harness = null

      if (config.BENCHMARK_TYPE == BenchmarkType.MEMORY) {
        harness = new MemoryHarness(log, config)
      } else if (config.BENCHMARK_TYPE == BenchmarkType.STARTUP) {
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
    var runs = 0
    var classname = ""
    var src: File = null
    var benchmarkDir: Directory = null
    var persistor: Directory = null
    var scalahome: Directory = null
    val separator = /*System.getProperty("file.separator")*/ "/"
    val extensionSeparator = "."
    var compile = true
    var logLevel = LogLevel.INFO

    def loop(args: List[String]) {
      args match {
        case opt :: rest => {
          argVal(args.head, args.tail)
        }
        case Nil => ()
      }

      def argVal(opt: String, rest: List[String]) {

        log debug opt

        opt match {
          case "--src" => {
            src = new File(new JFile(rest.head))
            benchmarkDir = new Directory(new JFile(rest.head.substring(0, rest.head lastIndexOf separator)))
            loop(rest.tail)
          }
          case "--scalahome" => {
            scalahome = new Directory(new JFile(rest.head))
            loop(rest.tail)
          }
          case "--help" => {
            printUsage(log)
            System exit 0
          }
          case "--persistor" => {
            try {
              persistor = new Directory(new JFile(rest.head))
              loop(rest.tail)
            } catch {
              case _ => {
                printUsage(log)
                System exit 1
              }
            }
          }
          case "--runs" => {
            try {
              runs = rest.head.toInt
              loop(rest.tail)
            } catch {
              case _ => {
                printUsage(log)
                System exit 1
              }
            }
          }
          case "--multiplier" => {
            try {
              multiplier = rest.head.toInt
              loop(rest.tail)
            } catch {
              case e => {
                log debug e.toString()
                printUsage(log)
                System exit 1
              }
            }
          }
          case "--noncompile" => {
            compile = false
            loop(rest)
          }
          case "--loglevel" => {
            rest.head match {
              case "debug" => logLevel = LogLevel.DEBUG
              case "verbose" => logLevel = LogLevel.VERBOSE
              case _ => ()
            }
            loop(rest.tail)
          }
          case some => rest match {
            case Nil => {
              classname = some
            }
            case _ => {
              log error "Options: " + opt
              printUsage(log)
              System exit 1
            }
          }
        }
      }
    }

    loop(args.toList)

    if ((classname equals "") || (src == null) || (scalahome == null) || (runs == 0)) {
      printUsage(log)
      System exit 1
    }
    if (multiplier == 0) {
      multiplier = 1
    }

    new Config(
      src,
      classname,
      benchmarkDir,
      (Path(benchmarkDir.path) / "build").createDirectory(),
      scalahome,
      separator,
      runs,
      multiplier,
      persistor,
      BenchmarkType.STARTUP,
      true,
      logLevel
    )
  }

  def printUsage(log: Log) {
    log yell "Usage: BenchmarkSuite --src <scala source file> --runs <runs> [Options] <MainClassName>"
    log yell "	Options: [--multiplier <multiplier>] [--noncompile] [--classdir <classdir>] [--help]"
    log yell "	The benchmark is warmed up <warm up> times, then run <runs> times, forcing a garbage collection between runs."
    log yell "	The optional -multiplier causes the benchmark to be repeated <multiplier> times, each time for <runs> executions."
    log yell "	The optional -noncompile causes the benchmark not to be recompiled."
    log yell "	The optional -classdir causes the generated class files to be placed at <classdir>"
    log yell "	The optional -help prints this usage."
  }

}
