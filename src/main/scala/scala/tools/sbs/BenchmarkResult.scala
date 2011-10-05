package scala.tools.sbs

import scala.tools.sbs.common.Benchmark

trait BenchmarkResult {

  def benchmark: Benchmark

}

trait BenchmarkSuccess extends BenchmarkResult {

  def mode: BenchmarkMode

}

abstract class BenchmarkFailure(benchmark: Benchmark) extends BenchmarkResult

case class CompileFailure(benchmark: Benchmark) extends BenchmarkFailure(benchmark)

case class ExceptionFailure(benchmark: Benchmark, mode: BenchmarkMode, exception: Exception)
  extends BenchmarkFailure(benchmark)
