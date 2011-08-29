package ndp.scala.benchmarksuite.utility

class Config(
  _SRC: String,
  _CLASSNAME: String,
  _CLASSPATH: String,
  _FILE_SEPARATOR: String,
  _RUNS: Int,
  _MULTIPLIER: Int,
  _PERSISTOR_LOC: String,
  _BENCHMARK_TYPE: BenchmarkType.Value,
  _COMPILE: Boolean) {

  def SRC = _SRC
  def CLASSNAME: String = _CLASSNAME
  def CLASSPATH = _CLASSPATH
  def FILE_SEPARATOR = _FILE_SEPARATOR
  def RUNS = _RUNS
  def MULTIPLIER = _MULTIPLIER
  def PERSISTOR_LOC = _PERSISTOR_LOC
  def BENCHMARK_TYPE = _BENCHMARK_TYPE
  def COMPILE = _COMPILE

  override def toString(): String = {
    "Config:" +
      "\n\tSource:			" + SRC +
      "\n\tClassname:		" + CLASSNAME +
      "\n\tClasspath:		" + CLASSPATH +
      "\n\tFileSeparator:		" + FILE_SEPARATOR +
      "\n\tRuns:			" + RUNS +
      "\n\tMultiplier:		" + MULTIPLIER +
      "\n\tPrevious result:	" + PERSISTOR_LOC +
      "\n\tBenchmarkType:		" + BENCHMARK_TYPE +
      "\n\tCompile:		" + COMPILE +
      "\n"
  }
}

object BenchmarkType extends Enumeration {
  type BenchmarType = Value
  val Startup, Steady, Memory = Value
}