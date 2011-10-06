package scala.tools.sbs

import scala.tools.sbs.common.Benchmark

trait BenchmarkResult {

  def benchmark: Benchmark

}

trait BenchmarkSuccess extends BenchmarkResult {

  def mode: BenchmarkMode

}

trait BenchmarkFailure extends BenchmarkResult

case class CompileFailure(benchmark: Benchmark) extends BenchmarkFailure

case class ExceptionFailure(benchmark: Benchmark, mode: BenchmarkMode, exception: Exception)
  extends BenchmarkFailure
