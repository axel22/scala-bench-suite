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

case class ExceptionFailure(benchmark: Benchmark, mode: BenchmarkMode, exception: Exception) extends BenchmarkFailure {

  def benchmarkName = benchmark.name

}
