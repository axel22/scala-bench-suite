/*
 * ArgumentParser
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs

import java.io.{ File => JFile }
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.GenericRunnerSettings
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.benchmark.BenchmarkKind.BenchmarkKind
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.PersistorFactory
import scala.tools.sbs.util.LogLevel.LogLevel
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.FileUtil
import scala.tools.sbs.util.Log
import scala.tools.sbs.util.LogFactory
import scala.tools.sbs.util.LogLevel
import scala.tools.sbs.util.UI
import scala.tools.sbs.benchmark.BenchmarkKind

/**
 * Parser for the suite's arguments.
 */
object ArgumentParser {

  /**
   * Holds string contants for parameters.
   */
  private object Parameter {

    val OPT_STEADY = "--steady-performance"
    val OPT_STARTUP = "--startup-performance"
    val OPT_MEMORY = "--memory-usage"
    val OPT_CLEAN = "--clean"
    val OPT_RUN_ALL = "--run-all"
    val OPT_NONCOMPILE = "--noncompile"
    val OPT_SHOWLOG = "--show-log"
    val OPT_HELP = "--help"

    val OPT_BENCHMARK_DIR = "--benchmark-dir"
    //    val OPT_SRCPATH = "--srcpath"
    val OPT_CLASSPATH = "--classpath"
    val OPT_SCALA_HOME = "--scala-home"
    val OPT_JAVA_HOME = "--java-home"
    val OPT_RUNS = "--runs"
    val OPT_MULTIPLIER = "--multiplier"
    val OPT_LOG_LEVEL = "--log-level"
    val OPT_PERSISTOR = "--persistor"
    val OPT_CREATE_SAMPLE = "--create-sample"

    def isUnary(opt: String) =
      (opt equals OPT_STEADY) ||
        (opt equals OPT_STARTUP) ||
        (opt equals OPT_MEMORY) ||
        (opt equals OPT_CLEAN) ||
        (opt equals OPT_RUN_ALL) ||
        (opt equals OPT_NONCOMPILE) ||
        (opt equals OPT_SHOWLOG) ||
        (opt equals OPT_HELP)

    def isBinary(opt: String) =
      (opt equals OPT_BENCHMARK_DIR) ||
        //        (opt equals OPT_SRCPATH) ||
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

    val slash = System getProperty "file.separator"
    val colon = System getProperty "path.separator"

    var root = "."
    var src = List[File]()
    var benchmarkName: String = null
    var benchmarkArguments = List[String]()
    var classpath: String = null
    var scalahome: Directory = null
    var javahome: Directory = null
    var persistorDir: Directory = null

    var modes = List[BenchmarkMode]()

    var clean = false

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
          } else if ((head startsWith "-") && (rest.length > 0)) {
            println(head)
            println(rest)
            exitOnError("Options: " + head)
          } else {
            benchmarkName = head
            benchmarkArguments = rest
          }
        }
        case Nil => ()
      }

      def parseBinary(opt: String, arg: String) = opt match {
        case Parameter.OPT_BENCHMARK_DIR => root = stripQuotes(arg)
        case Parameter.OPT_CLASSPATH => classpath = arg
        case Parameter.OPT_SCALA_HOME => scalahome = new Directory(new JFile(stripQuotes(arg)))
        case Parameter.OPT_JAVA_HOME => javahome = new Directory(new JFile(stripQuotes(arg)))
        case Parameter.OPT_PERSISTOR => persistorDir = new Directory(new JFile(stripQuotes(arg)))
        case Parameter.OPT_CREATE_SAMPLE => sampleNumber = parseInt(arg)
        case Parameter.OPT_RUNS => runs = parseInt(arg)
        case Parameter.OPT_MULTIPLIER => multiplier = parseInt(arg)
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
            exitOnError(Parameter.OPT_LOG_LEVEL + " " + arg)
          }
        }
      }

      def parseUnary(opt: String) = opt match {
        case Parameter.OPT_STEADY => modes ::= BenchmarkMode.STEADY
        case Parameter.OPT_STARTUP => modes ::= BenchmarkMode.STARTUP
        case Parameter.OPT_MEMORY => modes ::= BenchmarkMode.MEMORY
        case Parameter.OPT_CLEAN => clean = true
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

      def stripQuotes(s: String) =
        if (List('"', '\'') exists { c: Char => (s.length > 0 && c == s.head && c == s.last) }) s.tail.init else s

      def parseInt(i: String): Int = {
        try {
          i.toInt
        } catch {
          case _ => {
            exitOnError(Parameter.OPT_RUNS + " " + i)
            0
          }
        }
      }
    }

    loop(args.toList)

    if ((benchmarkName == null) || (benchmarkName equals "")) {
      exitOnError("No benchmark specified.")
    }
    if ((scalahome == null) || (scalahome equals "")) {
      exitOnError("No scala home specified.")
    }
    if (modes.length == 0) {
      exitOnError("No benchmark mode specified.")
    }

    val benchmarkdir = FileUtil.createBenchmarkDir(root, benchmarkName) match {
      case Left(dir) => dir
      case Right(err) => {
        exitOnError(err)
        null
      }
    }
    if (compile) {
      val srcdir = (benchmarkdir / "src").toDirectory
      src = srcdir.deepFiles.filter(_.hasExtension("scala")).foldLeft(src)((src, f) => f :: src)
      if (src.length == 0) {
        exitOnError("No source file specified.")
      }
    }

    if (persistorDir == null || !persistorDir.exists) {
      persistorDir = (benchmarkdir / "result").toDirectory
    } else if (!persistorDir.isDirectory || !persistorDir.canRead) {
      exitOnError("Persistor " + persistorDir.path + " inaccessible")
    }
    modes foreach (
      m => FileUtil.mkDir(persistorDir / m.toString) match {
        case Right(err) => exitOnError(err)
        case Left(dir) => FileUtil.mkDir(dir.path + slash + "FAILED") match {
          case Right(err) => exitOnError(err)
          case _ => ()
        }
      }

    )
    if (clean) {
      FileUtil.clean(benchmarkdir / "bin") match {
        case Some(err) => exitOnError(err)
        case _ => ()
      }
      modes foreach (
        m => FileUtil.clean(persistorDir / m.toString) match {
          case Some(err) => exitOnError(err)
          case _ => ()
        })

    }

    if (multiplier == 0 || multiplier == 1) {
      multiplier = 2
    }
    if (runs == 0) {
      runs = 1
    }
    if (javahome == null) {
      javahome = new Directory(new JFile(System getProperty "java.home"))
    }

    val jars = Directory(benchmarkdir / "lib").files filter (_.hasExtension("jar")) map (_.path)
    val classpathString = jars.foldLeft(classpath + colon + benchmarkdir.path + slash + "bin")((s, j) => s + colon + j)
    val settings = new GenericRunnerSettings(UI.error)
    settings.processArguments(List("-cp", classpathString), false)

    val config = new Config(
      Directory(benchmarkdir),
      runs,
      multiplier,
      sampleNumber,
      scalahome,
      javahome,
      compile)

    val log = (new LogFactory).create(benchmarkName, benchmarkdir, logLevel, showlog)

    val benchmark = new BenchmarkFactory(
      log, config, benchmarkName, benchmarkArguments, settings.classpathURLs, modes).create(
      BenchmarkKind.SNIPPET, benchmarkdir)

    return (config, log, benchmark)
  }

  def exitOnError(message: String) {
    UI.error(message)
    UI.printUsage
    System.exit(1)
  }

}
