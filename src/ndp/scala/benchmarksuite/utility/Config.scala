package ndp.scala.benchmarksuite
package utility

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

class Config(
  _SRC: File,
  _CLASSNAME: String,
  _BENCHMARK_DIR: Directory,
  _BENCHMARK_BUILD: Directory,
  _SCALA_HOME: Directory,
  _FILE_SEPARATOR: String,
  _RUNS: Int,
  _MULTIPLIER: Int,
  _PERSISTOR_LOC: Directory,
  _BENCHMARK_TYPE: BenchmarkType.Value,
  _COMPILE: Boolean,
  _LOG_LEVEL: LogLevel.Value) {

  lazy val SRC = _SRC
  lazy val CLASSNAME: String = _CLASSNAME
  lazy val BENCHMARK_DIR = _BENCHMARK_DIR
  lazy val BENCHMARK_BUILD = _BENCHMARK_BUILD
  lazy val SCALA_HOME = _SCALA_HOME
  lazy val FILE_SEPARATOR = _FILE_SEPARATOR
  lazy val RUNS = _RUNS
  lazy val MULTIPLIER = _MULTIPLIER
  lazy val PERSISTOR_LOC = _PERSISTOR_LOC
  lazy val BENCHMARK_TYPE = _BENCHMARK_TYPE
  lazy val COMPILE = _COMPILE
  lazy val LOG_LEVEL = _LOG_LEVEL
  lazy val JAVACMD = "java"
  lazy val JAVAPROP = "-Dscala.home=" + _SCALA_HOME
  lazy val SCALA_LIB: String = {
    var lib: List[String] = List()
    val libpath = (SCALA_HOME / "lib").createDirectory()
    for (file <- libpath.files) {
      lib ::= file.path
    }
    lib mkString ";"
  }

  override def toString(): String = {
    "Config:" +
      "\n\t\t\tSource:			" + SRC +
      "\n\t\t\tClassname:		" + CLASSNAME +
      "\n\t\t\tBenchmarkDir:		" + BENCHMARK_DIR.path +
      "\n\t\t\tBuild:			" + BENCHMARK_BUILD.path +
      "\n\t\t\tScala home:		" + SCALA_HOME +
      "\n\t\t\tFileSeparator:		" + FILE_SEPARATOR +
      "\n\t\t\tRuns:			" + RUNS +
      "\n\t\t\tMultiplier:		" + MULTIPLIER +
      "\n\t\t\tPrevious result:	" + PERSISTOR_LOC +
      "\n\t\t\tBenchmarkType:		" + BENCHMARK_TYPE +
      "\n\t\t\tCompile:		" + COMPILE +
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