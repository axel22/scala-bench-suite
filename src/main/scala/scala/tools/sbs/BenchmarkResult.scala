package scala.tools.sbs

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkInfo

trait BenchmarkResult {

  def benchmarkName: String

}

trait BenchmarkSuccess extends BenchmarkResult {

  def mode: BenchmarkMode

}

trait BenchmarkFailure extends BenchmarkResult

case class CompileFailure(info: BenchmarkInfo) extends BenchmarkFailure {

  def benchmarkName = info.name

}

case class ExceptionFailure(benchmarkName: String, exception: Exception) extends BenchmarkFailure
