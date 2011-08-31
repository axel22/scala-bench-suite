package ndp.scala.benchmarksuite
package utility

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

class Config(
  _SRC: File,
  _CLASSNAME: String,
  _BENCHMARK_DIR: Directory,
  _FILE_SEPARATOR: String,
  _RUNS: Int,
  _MULTIPLIER: Int,
  _PERSISTOR_LOC: String,
  _BENCHMARK_TYPE: BenchmarkType.Value,
  _COMPILE: Boolean,
  _LOG_LEVEL: LogLevel.Value) {

  def SRC = _SRC
  def CLASSNAME: String = _CLASSNAME
  def BENCHMARK_DIR = _BENCHMARK_DIR
  def FILE_SEPARATOR = _FILE_SEPARATOR
  def RUNS = _RUNS
  def MULTIPLIER = _MULTIPLIER
  def PERSISTOR_LOC = _PERSISTOR_LOC
  def BENCHMARK_TYPE = _BENCHMARK_TYPE
  def COMPILE = _COMPILE
  def LOG_LEVEL = _LOG_LEVEL

  override def toString(): String = {
    "Config:" +
      "\n\t\tSource:			" + SRC +
      "\n\t\tClassname:		" + CLASSNAME +
      "\n\t\tClassdir:		" + BENCHMARK_DIR.path +
      "\n\t\tFileSeparator:		" + FILE_SEPARATOR +
      "\n\t\tRuns:			" + RUNS +
      "\n\t\tMultiplier:		" + MULTIPLIER +
      "\n\t\tPrevious result:	" + PERSISTOR_LOC +
      "\n\t\tBenchmarkType:		" + BENCHMARK_TYPE +
      "\n\t\tCompile:		" + COMPILE +
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