/*
 * RegressionResult
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Constant

/** Represents the result of a benchmarking (of one benchmark on one {@link BenchmarkMode}).
 */
trait RegressionResult extends BenchmarkResult

abstract class RegressionSuccess(benchmark: Benchmark)
  extends RegressionResult
  with BenchmarkSuccess {

  def confidenceLevel: Int

  def benchmarkName = benchmark.name

}

trait RegressionDetected {

  def current: (Double, Double)

  def previous: ArrayBuffer[(Double, Double)]

  def toReport = ArrayBuffer(
    Constant.INDENT + "New approach sample mean: " + current._1.formatted("%.2f") +
      " +- " + current._2.formatted("%.2f"),
    Constant.INDENT + "History sample mean:      ") ++
    (previous./:(ArrayBuffer[String]())((lines, m) => lines :+
      Constant.INDENT + "                          " + m._1.formatted("%.2f") +
      " +- " + m._2.formatted("%.2f")))

}

trait CIRegression extends RegressionDetected {

  def CI: (Double, Double)

  override def toReport = super.toReport :+
    (Constant.INDENT + "Confidence interval:      [" + CI._1.formatted("%.2f") + "; " + CI._2.formatted("%.2f") + "]")
}

trait ANOVARegression extends RegressionDetected {

  def SSA: Double

  def SSE: Double

  def FValue: Double

  def F: Double

  override def toReport =
    super.toReport :+
      ("         F-test:") :+
      (" Sum-of-squared due to alternatives: " + SSA) :+
      ("       Sum-of-squared due to errors: " + SSE) :+
      ("                       Alternatives: " + "K") :+
      ("     Each alternatives measurements: " + "N") :+
      ("SSA * (N - 1) * K / (SSE * (K - 1)): " + FValue) :+
      ("                     F distribution: " + F)

}

case class CIRegressionSuccess(benchmark: Benchmark,
                               confidenceLevel: Int,
                               current: (Double, Double),
                               previous: ArrayBuffer[(Double, Double)],
                               CI: (Double, Double))
  extends RegressionSuccess(benchmark)
  with CIRegression

case class ANOVARegressionSuccess(benchmark: Benchmark,
                                  confidenceLevel: Int,
                                  current: (Double, Double),
                                  previous: ArrayBuffer[(Double, Double)],
                                  SSA: Double,
                                  SSE: Double,
                                  FValue: Double,
                                  F: Double)
  extends RegressionSuccess(benchmark)
  with ANOVARegression

case class NoPreviousMeasurement(benchmark: Benchmark, measurementSuccess: MeasurementSuccess)
  extends RegressionResult with BenchmarkSuccess {

  def benchmarkName = benchmark.name

  def toReport = ArrayBuffer(Constant.INDENT + "No previous measurement result to detect regression")

}

abstract class RegressionFailure(benchmark: Benchmark) extends RegressionResult with BenchmarkFailure {

  def benchmarkName = benchmark.name

}

case class CIRegressionFailure(benchmark: Benchmark,
                               current: (Double, Double),
                               previous: ArrayBuffer[(Double, Double)],
                               CI: (Double, Double))
  extends RegressionFailure(benchmark)
  with CIRegression

case class ANOVARegressionFailure(benchmark: Benchmark,
                                  current: (Double, Double),
                                  previous: ArrayBuffer[(Double, Double)],
                                  SSA: Double,
                                  SSE: Double,
                                  FValue: Double,
                                  F: Double)
  extends RegressionFailure(benchmark)
  with ANOVARegression

case class ImmeasurableFailure(benchmark: Benchmark, failure: MeasurementFailure)
  extends RegressionFailure(benchmark) {

  def toReport = ArrayBuffer(Constant.INDENT + failure.reason)

}
