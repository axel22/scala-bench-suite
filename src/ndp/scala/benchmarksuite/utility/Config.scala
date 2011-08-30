package ndp.scala.benchmarksuite.utility

class Config(
  _SRC: String,
  _CLASSNAME: String,
  _BENCHMARK_DIR: String,
  _FILE_SEPARATOR: String,
  _RUNS: Int,
  _MULTIPLIER: Int,
  _PERSISTOR_LOC: String,
  _BENCHMARK_TYPE: BenchmarkType.Value,
  _COMPILE: Boolean) {

  def SRC = _SRC
  def CLASSNAME: String = _CLASSNAME
  def BENCHMARK_DIR = _BENCHMARK_DIR
  def FILE_SEPARATOR = _FILE_SEPARATOR
  def RUNS = _RUNS
  def MULTIPLIER = _MULTIPLIER
  def PERSISTOR_LOC = _PERSISTOR_LOC
  def BENCHMARK_TYPE = _BENCHMARK_TYPE
  def COMPILE = _COMPILE

  override def toString(): String = {
    "Config:" +
      "\n\t\tSource:			" + SRC +
      "\n\t\tClassname:		" + CLASSNAME +
      "\n\t\tClasspath:		" + BENCHMARK_DIR +
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
  val Startup, Steady, Memory = Value
}