/*
 * ParameterParser
 * 
 * Version
 * 
 * Created on August 31st 2011
 * 
 * Created by ND P
 */
package ndp.scala.benchmarksuite

import java.io.{ File => JFile }

import scala.annotation.implicitNotFound
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel

/**
 * Parser for the suite's arguments.
 */
object ArgumentParser {

  /**
   * Parses the arguments from command line.
   * 
   * @return	The `Config` object conresponding for the parsed values
   */
  def parse(args: Array[String], log: Log, printUsage: Log => Unit): Config = {
    var benchmarkDir: Directory = null
    var srcpath: File = null
    var classname = ""
    var scalahome: Directory = null
    var javahome: Directory = null

    var persistor: Directory = null
    val separator = System getProperty "file.separator"

    var multiplier = 0
    var runs = 0

    var compile = true
    var logLevel = LogLevel.INFO
    var showlog = false

    /**
     * Loop through a `List[String]` containing the arguments.
     * Gets the first argument and recursive loop through the rest.
     */
    def loop(args: List[String]) {
      args match {
        case opt :: rest => {
          if (Parameter isUnary opt) {
            parseUnary(opt)
            loop(rest)
          } else if (Parameter isBinary opt) {
            parseBinary(opt, rest.head)
            loop(rest.tail)
          } else if ((opt startsWith "--") || (rest.length > 0)) {
            log error "Options: " + opt
            printUsage(log)
            System exit 1
          } else {
            classname = opt
          }
        }
        case Nil => ()
      }

      def parseBinary(opt: String, arg: String) {
        opt match {
          case Parameter.OPT_BENCHMARK_DIR => {
            benchmarkDir = new Directory(new JFile(arg))
            try {
              benchmarkDir createDirectory ()
            } catch {
              case _ => {
                log error "Cannot create directory: " + benchmarkDir.path
                System exit 1
              }
            }
          }
          case Parameter.OPT_SRC_PATH => {
            srcpath = new File(new JFile(arg))
          }
          case Parameter.OPT_SCALA_HOME => {
            scalahome = new Directory(new JFile(arg))
          }
          case Parameter.OPT_JAVA_HOME => {
            javahome = new Directory(new JFile(arg))
          }
          case Parameter.OPT_PERSISTOR => {
            persistor = new Directory(new JFile(arg))
          }
          case Parameter.OPT_RUNS => {
            try {
              runs = arg.toInt
            } catch {
              case _ => {
                log error Parameter.OPT_RUNS + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
          case Parameter.OPT_MULTIPLIER => {
            try {
              multiplier = arg.toInt
            } catch {
              case _ => {
                log error Parameter.OPT_MULTIPLIER + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
          case Parameter.OPT_LOG_LEVEL => {
            arg match {
              case "debug" => logLevel = LogLevel.DEBUG
              case "verbose" => logLevel = LogLevel.VERBOSE
              case "info" => logLevel = LogLevel.INFO
              case _ => {
                log error Parameter.OPT_MULTIPLIER + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
        }
      }

      def parseUnary(opt: String) {
        opt match {
          case Parameter.OPT_HELP => {
            printUsage(log)
            System exit 0
          }
          case Parameter.OPT_SHOWLOG => {
            showlog = true
          }
          case Parameter.OPT_NONCOMPILE => {
            compile = false
          }
        }
      }
    }

    loop(args.toList)

    if ((classname equals "") || (srcpath == null) || (scalahome == null) || (runs == 0)) {
      printUsage(log)
      System exit 1
    }
    if (multiplier == 0) {
      multiplier = 1
    }

    new Config(
      benchmarkDir,
      srcpath,
      classname,
      scalahome,
      javahome,
      separator,
      runs,
      multiplier,
      persistor,
      BenchmarkType.STARTUP,
      compile,
      logLevel,
      showlog
    )
  }
}

/**
 * Holds string contants for parameters.
 */
object Parameter {

  val OPT_NONCOMPILE = "--noncompile"
  val OPT_SHOWLOG = "--showlog"
  val OPT_HELP = "--help"

  val OPT_BENCHMARK_DIR = "--benchmarkdir"
  val OPT_SRC_PATH = "--srcpath"
  val OPT_SCALA_HOME = "--scalahome"
  val OPT_JAVA_HOME = "--javahome"
  val OPT_RUNS = "--runs"
  val OPT_MULTIPLIER = "--multiplier"
  val OPT_LOG_LEVEL = "--loglevel"
  val OPT_PERSISTOR = "--persistor"

  def isBinary(opt: String): Boolean = {
    (opt equals OPT_NONCOMPILE) ||
      (opt equals OPT_SHOWLOG) ||
      (opt equals OPT_HELP)
  }

  def isUnary(opt: String): Boolean = {
    (opt equals OPT_BENCHMARK_DIR) ||
      (opt equals OPT_SRC_PATH) ||
      (opt equals OPT_SCALA_HOME) ||
      (opt equals OPT_JAVA_HOME) ||
      (opt equals OPT_RUNS) ||
      (opt equals OPT_MULTIPLIER) ||
      (opt equals OPT_LOG_LEVEL) ||
      (opt equals OPT_PERSISTOR)
  }

}
