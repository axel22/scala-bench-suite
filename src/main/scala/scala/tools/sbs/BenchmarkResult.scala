package scala.tools.sbs

import scala.Array.canBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.util.Constant

trait BenchmarkResult {

  def benchmarkName: String

  def toReport: ArrayBuffer[String]

}

trait BenchmarkSuccess extends BenchmarkResult

trait BenchmarkFailure extends BenchmarkResult

case class CompileBenchmarkFailure(info: BenchmarkInfo) extends BenchmarkFailure {

  def benchmarkName = info.name

  def toReport = ArrayBuffer(Constant.INDENT + "Compiling benchmark failed")

}

case class ExceptionBenchmarkFailure(benchmarkName: String, exception: Exception) extends BenchmarkFailure {

  def toReport =
    ArrayBuffer((exception.toString split "\n" map (Constant.INDENT + _)) ++
      (exception.getStackTraceString split "\n" map (Constant.INDENT + "  " + _)): _*)

}
