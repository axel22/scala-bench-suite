/*
 * ArgumentParser
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs

import java.io.{ File => JFile }
import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import ndp.scala.tools.sbs.util.LogLevel.LogLevel
import ndp.scala.tools.sbs.util.BenchmarkType
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.LogLevel
import ndp.scala.tools.sbs.util.UI
import scala.tools.nsc.GenericRunnerSettings

/**
 * Parser for the suite's arguments.
 */
object ArgumentParser {

  /**
   * Holds string contants for parameters.
   */
  private object Parameter {

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
    val OPT_CREATE_SAMPLE = "--create-sample"

    def isUnary(opt: String) =
      (opt equals OPT_NONCOMPILE) ||
        (opt equals OPT_SHOWLOG) ||
        (opt equals OPT_HELP)

    def isBinary(opt: String) =
      (opt equals OPT_BENCHMARK_DIR) ||
        (opt equals OPT_SRCPATH) ||
        (opt equals OPT_CLASSPATH) ||
        (opt equals OPT_SCALA_HOME) ||
        (opt equals OPT_JAVA_HOME) ||
        (opt equals OPT_RUNS) ||
        (opt equals OPT_MULTIPLIER) ||
        (opt equals OPT_LOG_LEVEL) ||
        (opt equals OPT_PERSISTOR) ||
        (opt equals OPT_CREATE_SAMPLE)

    def isOption(opt: String) = isUnary(opt) || isBinary(opt)

  }

  /**
   * Parses the arguments from command line.
   *
   * @return	The `Config` object conresponding for the parsed values
   */
  def parse(args: Array[String]): (Config, Log, Benchmark) = {

    val separator = System getProperty "file.separator"

    var benchmarkdir: Directory = Path(".").toDirectory
    var srcpath: File = null
    var benchmarkName: String = null
    var benchmarkArguments = List[String]()
    var classpath = "."
    var benchmarkBuild: Directory = null
    var scalahome: Directory = null
    var javahome: Directory = null
    var persistor: Directory = null

    var sampleNumber = 0
    var multiplier = 0
    var runs = 0

    var compile = true
    var logLevel: LogLevel = LogLevel.INFO
    var showlog = false

    /**
     * Loop through a `List[String]` containing the arguments.
     * Gets the first argument and recursive loop through the rest.
     */
    def loop(args: List[String]) {
      args match {
        case head :: rest => {
          if (Parameter isUnary head) {
            parseUnary(head)
            loop(rest)
          } else if (Parameter isBinary head) {
            parseBinary(head, rest.head)
            loop(rest.tail)
          } else if ((head startsWith "-") || (rest.length > 0)) {
            UI.error("Options: " + head)
            UI.printUsage()
            System.exit(1)
          } else {
            benchmarkName = head
            benchmarkArguments = rest
          }
        }
        case Nil => ()
      }

      def parseBinary(opt: String, arg: String) {
        opt match {
          case Parameter.OPT_BENCHMARK_DIR => {
            benchmarkdir = new Directory(new JFile(stripQuotes(arg)))
            benchmarkBuild = new Directory(new JFile(stripQuotes(arg) + separator + "build"))
            try {
              benchmarkdir.createDirectory()
              benchmarkBuild.createDirectory()
            } catch {
              case _ => {
                UI.error("Cannot create directory: " + benchmarkdir.path)
                UI.printUsage
                System.exit(1)
              }
            }
          }
          case Parameter.OPT_SRCPATH => {
            srcpath = new File(new JFile(stripQuotes(arg)))
          }
          case Parameter.OPT_CLASSPATH => {
            classpath = arg
          }
          case Parameter.OPT_SCALA_HOME => {
            scalahome = new Directory(new JFile(stripQuotes(arg)))
          }
          case Parameter.OPT_JAVA_HOME => {
            javahome = new Directory(new JFile(stripQuotes(arg)))
          }
          case Parameter.OPT_PERSISTOR => {
            persistor = new Directory(new JFile(stripQuotes(arg)))
            if (!persistor.exists) {
              persistor.createDirectory()
            } else if (!persistor.isDirectory || !persistor.canRead) {
              UI.error(persistor.path + " inaccessible")
              UI.printUsage
              System.exit(1)
            }
          }
          case Parameter.OPT_CREATE_SAMPLE => {
            sampleNumber = parseInt(arg)
          }
          case Parameter.OPT_RUNS => {
            runs = parseInt(arg)
          }
          case Parameter.OPT_MULTIPLIER => {
            multiplier = parseInt(arg)
          }
          case Parameter.OPT_LOG_LEVEL => {
            if (arg equals LogLevel.DEBUG.toString()) {
              logLevel = LogLevel.DEBUG
            } else if (arg equals LogLevel.VERBOSE.toString()) {
              logLevel = LogLevel.VERBOSE
            } else if (arg equals LogLevel.INFO.toString()) {
              logLevel = LogLevel.INFO
            } else if (arg equals LogLevel.ALL.toString()) {
              logLevel = LogLevel.ALL
            } else {
              UI.error(Parameter.OPT_LOG_LEVEL + " " + arg)
              UI.printUsage
              System.exit(1)
            }
          }
        }
      }

      def parseUnary(opt: String) {
        opt match {
          case Parameter.OPT_HELP => {
            UI.printUsage
            System.exit(0)
          }
          case Parameter.OPT_SHOWLOG => {
            showlog = true
          }
          case Parameter.OPT_NONCOMPILE => {
            compile = false
          }
        }
      }

      def stripQuotes(s: String) =
        if (List('"', '\'') exists { c: Char => (s.length > 0 && c == s.head && c == s.last) }) s.tail.init else s

      def parseInt(i: String): Int = {
        try {
          i.toInt
        } catch {
          case _ => {
            UI.error(Parameter.OPT_RUNS + " " + i)
            UI.printUsage
            System.exit(1)
            0
          }
        }
      }
    }

    loop(args.toList)

    if (compile && srcpath == null) {
      UI.printUsage()
      System.exit(1)
    }
    if ((benchmarkName == null) || (scalahome == null) || (runs == 0)) {
      UI.printUsage
      System.exit(1)
    }
    if (multiplier == 0 || multiplier == 1) {
      multiplier = 2
    }
    if (javahome == null) {
      javahome = new Directory(new JFile(System getProperty "java.home"))
    }
    if (persistor == null) {
      persistor = (Path(benchmarkdir.path) / "persistor").createDirectory()
    }

    val settings = new GenericRunnerSettings(log.error)
    settings.processArguments(List("-cp", classpath + (System getProperty "path.separator") + benchmarkBuild.path), false)

    return (
      new Config(
        benchmarkdir,
        BenchmarkType.STARTUP,
        runs,
        multiplier,
        scalahome,
        javahome,
        classpath,
        persistor,
        sampleNumber,
        compile),
      new Log(
        Log.createLog(benchmarkdir, benchmarkName) match {
          case Some(file) => file
          case None => null
        },
        logLevel,
        showlog),
      new Benchmark(
        benchmarkName,
        benchmarkArguments,
        settings.classpathURLs,
        srcpath,
        benchmarkBuild))
  }
}
