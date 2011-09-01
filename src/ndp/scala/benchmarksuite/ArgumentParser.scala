/*
 * ArgumentParser
 * 
 * Version
 * 
 * Created on August 31st 2011
 * 
 * Created by ND P
 */
package ndp.scala.benchmarksuite

import java.io.{ File => JFile }
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel
import scala.collection.mutable.ArrayBuffer

object ArgumentParser {

  def parse(args: Array[String], log: Log, printUsage: Log => Unit): Config = {
    var multiplier = 0
    var runs = 0
    var classname = ""
    var srcpath: File = null
    var benchmarkDir: Directory = null
    var persistor: Directory = null
    var scalahome: Directory = null
    var javahome: Directory = null
    val separator = /*System.getProperty("file.separator")*/ "/"
    val extensionSeparator = "."
    var compile = true
    var logLevel = LogLevel.INFO
    var showlog = false

    def loop(args: List[String]) {
      args match {
        case opt :: rest => {
          if (Argument isUnary opt) {
            getUnary(opt)
            loop(rest)
          } else if (Argument isBinary opt) {
            getBinary(opt, rest.head)
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

      def getBinary(opt: String, arg: String) {
        opt match {
          case Argument.OPT_BENCHMARK_DIR => {
            benchmarkDir = new Directory(new JFile(arg))
            //TODDO check
          }
          case Argument.OPT_SRC_PATH => {
            srcpath = new File(new JFile(arg))
          }
          case Argument.OPT_SCALA_HOME => {
            scalahome = new Directory(new JFile(arg))
            //TODO check
          }
          case Argument.OPT_JAVA_HOME => {
            javahome = new Directory(new JFile(arg))
            //TODO check
          }
          case Argument.OPT_PERSISTOR => {
            try {
              persistor = new Directory(new JFile(arg))
              //TODO check
            } catch {
              case _ => {
                printUsage(log)
                System exit 1
              }
            }
          }
          case Argument.OPT_RUNS => {
            try {
              runs = arg.toInt
            } catch {
              case _ => {
                log error Argument.OPT_RUNS + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
          case Argument.OPT_MULTIPLIER => {
            try {
              multiplier = arg.toInt
            } catch {
              case _ => {
                log error Argument.OPT_MULTIPLIER + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
          case Argument.OPT_LOG_LEVEL => {
            arg match {
              case "debug" => logLevel = LogLevel.DEBUG
              case "verbose" => logLevel = LogLevel.VERBOSE
              case "info" => logLevel = LogLevel.INFO
              case _ => {
                log error Argument.OPT_MULTIPLIER + " " + arg
                printUsage(log)
                System exit 1
              }
            }
          }
        }
      }

      def getUnary(opt: String) {
        opt match {
          case Argument.OPT_HELP => {
            printUsage(log)
            System exit 0
          }
          case Argument.OPT_SHOWLOG => {
            showlog = true
          }
          case Argument.OPT_NONCOMPILE => {
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

class Argument private[benchmarksuite] {

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

object Argument extends Argument
