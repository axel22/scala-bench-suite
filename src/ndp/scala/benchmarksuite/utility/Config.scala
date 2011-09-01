package ndp.scala.benchmarksuite
package utility

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

class Config(
  _BENCHMARK_DIR: Directory,
  _SRCPATH: File,
  _CLASSNAME: String,
  _BENCHMARK_BUILD: Directory,
  _SCALA_HOME: Directory,
  _JAVA_HOME: Directory,
  _FILE_SEPARATOR: String,
  _RUNS: Int,
  _MULTIPLIER: Int,
  _PERSISTOR_LOC: Directory,
  _BENCHMARK_TYPE: BenchmarkType.Value,
  _COMPILE: Boolean,
  _LOG_LEVEL: LogLevel.Value,
  _SHOW_LOG: Boolean) {

  val SRCPATH = _SRCPATH
  val CLASSNAME: String = _CLASSNAME
  val BENCHMARK_DIR = _BENCHMARK_DIR
  val BENCHMARK_BUILD = _BENCHMARK_BUILD
  val SCALA_HOME = _SCALA_HOME
  val JAVA_HOME = _JAVA_HOME
  val FILE_SEPARATOR = _FILE_SEPARATOR
  val RUNS = _RUNS
  val MULTIPLIER = _MULTIPLIER
  val PERSISTOR_LOC = _PERSISTOR_LOC
  val BENCHMARK_TYPE = _BENCHMARK_TYPE
  val COMPILE = _COMPILE
  val LOG_LEVEL = _LOG_LEVEL
  val SHOW_LOG = _SHOW_LOG
  lazy val JAVACMD = JAVA_HOME + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "java"
  lazy val JAVAPROP = "-Dscala.home=" + _SCALA_HOME
  lazy val SCALA_LIB: String = {
    var lib: List[String] = List()
    val libpath = (SCALA_HOME / "lib").createDirectory()
    for (file <- libpath.files) {
      lib ::= file.path
    }
    lib mkString (System getProperty "path.separator")
  }

  override def toString(): String = {
    "Config:" +
      "\n        Source:          " + SRCPATH.path +
      "\n        Classname:       " + CLASSNAME +
      "\n        BenchmarkDir:    " + BENCHMARK_DIR.path +
      "\n        Scala home:      " + SCALA_HOME.path +
      "\n        Java home:       " + JAVA_HOME.path +
      "\n        FileSeparator:   " + FILE_SEPARATOR +
      "\n        Runs:            " + RUNS +
      "\n        Multiplier:      " + MULTIPLIER +
      "\n        Previous result: " + PERSISTOR_LOC.path +
      "\n        BenchmarkType:   " + BENCHMARK_TYPE +
      "\n        Compile:         " + COMPILE +
      "\n        Log level:       " + LOG_LEVEL +
      "\n        Show log:        " + SHOW_LOG +
      "\n        Java:            " + JAVACMD +
      "\n        Java properties: " + JAVAPROP +
      "\n        Scala library:   " + SCALA_LIB +
      "\n"
  }
}

object BenchmarkType extends Enumeration {
  type BenchmarType = Value
  val STARTUP, STEADY, MEMORY = Value
}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE = Value
}
