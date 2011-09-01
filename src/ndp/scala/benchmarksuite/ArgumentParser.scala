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

import java.io.{File => JFile}
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.LogLevel
import ndp.scala.benchmarksuite.utility.UI
import scala.tools.nsc.io.Path

/**
 * Parser for the suite's arguments.
 */
object ArgumentParser {

  /**
   * Parses the arguments from command line.
   *
   * @return	The `Config` object conresponding for the parsed values
   */
  def parse(args: Array[String]): Config = {
    var benchmarkdir: Directory = null
    var srcpath: File = null
    var classname = ""
    var classpath = ""
    var benchmarkBuild: Directory = null
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
            UI error "Options: " + opt
            UI.printUsage
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
            benchmarkdir = new Directory(new JFile(arg))
            benchmarkBuild = new Directory(new JFile(arg + separator + "build"))
            try {
              benchmarkdir createDirectory ()
              benchmarkBuild createDirectory ()
            } catch {
              case _ => {
                UI error "Cannot create directory: " + benchmarkdir.path
                UI.printUsage
                System exit 1
              }
            }
          }
          case Parameter.OPT_SRCPATH => {
            srcpath = new File(new JFile(arg))
          }
          case Parameter.OPT_CLASSPATH => {
            classpath = arg
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
                UI error Parameter.OPT_RUNS + " " + arg
                UI.printUsage
                System exit 1
              }
            }
          }
          case Parameter.OPT_MULTIPLIER => {
            try {
              multiplier = arg.toInt
            } catch {
              case _ => {
                UI error Parameter.OPT_MULTIPLIER + " " + arg
                UI.printUsage
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
                UI error Parameter.OPT_MULTIPLIER + " " + arg
                UI.printUsage
                System exit 1
              }
            }
          }
        }
      }

      def parseUnary(opt: String) {
        opt match {
          case Parameter.OPT_HELP => {
            UI.printUsage
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

    if (benchmarkdir == null) {
      UI.printUsage
      System exit 1
    }
    if ((classname equals "") || (srcpath == null) || (scalahome == null) || (runs == 0)) {
      UI.printUsage
      System exit 1
    }
    if (multiplier == 0) {
      multiplier = 1
    }
    if (javahome == null) {
      javahome = new Directory(new JFile(System getProperty "java.home"))
    }
    if (persistor == null) {
      persistor = (Path(benchmarkdir.path) / "persistor").createDirectory()
    }

    new Config(
      benchmarkdir,
      srcpath,
      classname,
      benchmarkBuild,
      scalahome,
      javahome,
      separator,
      runs,
      multiplier,
      persistor,
      BenchmarkType.MEMORY,
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
  val OPT_SHOWLOG = "--show-log"
  val OPT_HELP = "--help"

  val OPT_BENCHMARK_DIR = "--benchmark-dir"
  val OPT_SRCPATH = "--srcpath"
  val OPT_CLASSPATH = "--classpath"
  val OPT_SCALA_HOME = "--scala-home"
  val OPT_JAVA_HOME = "--java-home"
  val OPT_RUNS = "--runs"
  val OPT_MULTIPLIER = "--multiplier"
  val OPT_LOG_LEVEL = "--log-level"
  val OPT_PERSISTOR = "--persistor"

  def isUnary(opt: String): Boolean = {
    (opt equals OPT_NONCOMPILE) ||
      (opt equals OPT_SHOWLOG) ||
      (opt equals OPT_HELP)
  }

  def isBinary(opt: String): Boolean = {
    (opt equals OPT_BENCHMARK_DIR) ||
      (opt equals OPT_SRCPATH) ||
      (opt equals OPT_CLASSPATH) ||
      (opt equals OPT_SCALA_HOME) ||
      (opt equals OPT_JAVA_HOME) ||
      (opt equals OPT_RUNS) ||
      (opt equals OPT_MULTIPLIER) ||
      (opt equals OPT_LOG_LEVEL) ||
      (opt equals OPT_PERSISTOR)
  }

}
