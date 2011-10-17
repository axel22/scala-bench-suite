/*
 * SimpleStatistic
 * 
 * Version
 * 
 * Created on September, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.math.sqrt
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Constant.LEAST_CONFIDENCE_LEVEL
import scala.tools.sbs.BenchmarkMode

import org.apache.commons.math.distribution.FDistributionImpl
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl

/** An simple implement of {@link Statistics}.
 */
class SimpleStatistics(log: Log, var alpha: Double = 0) extends Statistics {

  /** Reduces the confidence level time by time to by 5% each time,
   *  except 2 cases:
   *  <ul>
   *  <li>From 100% to 99%
   *  <li>From 99% to 95%
   *  </ul>
   *
   *  @return `true` if success, `false` if the confidence leval is at the minimum value.
   */
  def reduceConfidenceLevel(): Int = {
    if (confidenceLevel == 100) {
      alpha = 0.01
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
    }
    else if (confidenceLevel == 99) {
      alpha = 0.05
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
    }
    else if (confidenceLevel >= LEAST_CONFIDENCE_LEVEL) {
      alpha += 0.05
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
    }
    else {
      log.verbose("Confidence level is not reducible")
    }
    confidenceLevel
  }

  /** @return `true` if the confidence level is GE `1 - MAX_ALPHA`, `false` otherwise
   */
  def isConfidenceLevelAcceptable = if (confidenceLevel >= LEAST_CONFIDENCE_LEVEL) true else false

  def resetConfidenceInterval() {
    alpha = 0
  }

  /** Computes the confidence interval for the given sample.
   *
   *  @param series	The result of benchmarking
   *
   *  @return	The left and right end points of the confidence interval.
   */
  def confidenceInterval(series: Series): (Double, Double) = {
    val SD = standardDeviation(series)
    var diff: Double = 0

    if (SD == 0) {
      diff = 0
    }
    else if (series.length >= 30) {
      diff = inverseGaussianDistribution * SD / sqrt(series.length)
    }
    else {
      diff = inverseStudentDistribution(series.length - 1) * SD / sqrt(series.length)
    }
    (mean(series) - diff, mean(series) + diff)
  }

  /** Computes z value of the standard normal distribution with mean 0 and variance 1.
   *
   *  @param alpha	The significant level
   *
   *  @return	The z value
   */
  private def inverseGaussianDistribution() =
    new NormalDistributionImpl(0, 1).inverseCumulativeProbability(1 - alpha / 2)

  /** Computes the t value of the Student distribution using:
   *  <ul>
   *  <li>A given degree of freedom
   *  <li>The pre-defined significant level
   *  </ul>
   *
   *  @param df	The degree of freedom
   *
   *  @return	The t value
   */
  private def inverseStudentDistribution(df: Int) =
    new TDistributionImpl(df).inverseCumulativeProbability(1 - alpha / 2)

  /** Computes the F value of the Fisher F distribution using:
   *  <ul>
   *  <li>Given degrees of freedom
   *  <li>The pre-defined significant level
   *  </ul>
   *
   *  @param n1	The first degree of freedom
   *  @param n2	The second degree of freedom
   *
   *  @return	The F value
   */
  private def inverseFDistribution(n1: Int, n2: Int) =
    new FDistributionImpl(n1, n2).inverseCumulativeProbability(1 - alpha)

  /** @param series	The result of benchmarking
   *
   *  @return	The minimum value
   */
  def min(series: Series) = series.foldLeft(series.head) { (min, s) => if (min < s) min else s }

  /** @param series	The result of benchmarking
   *
   *  @return	The maximum value
   */
  def max(series: Series) = series.foldRight(series.last) { (max, s) => if (max > s) max else s }

  /** Computes the sample mean.
   *
   *  @param series	The result of benchmarking
   *
   *  @return	The average
   */
  def mean(series: Series) =
    if (series.length == 0) 0 else series.foldLeft(0: Double) { (sum: Double, s) => sum + s } / series.length

  /** Computes the standard deviation of a given sample.
   *
   *  @param series	The result of benchmarking
   *
   *  @return	The standard deviation
   */
  def standardDeviation(series: Series): Double = {
    if (series.length == 0) {
      0
    }
    else {
      val mean = this.mean(series)
      sqrt(series.foldLeft(0: Double) { (squareSum, s) => squareSum + (s - mean) * (s - mean) } / (series.length - 1))
    }
  }

  /** Computes the coefficient of variation of a given sample.
   *
   *  @param series	The result of benchmarking
   *
   *  @return	The coefficient of variation
   */
  def CoV(series: Series) = standardDeviation(series) / mean(series)

  /** @return	The significant level alpha
   */
  def significantLevel = alpha

  /** @return	The confident level
   */
  def confidenceLevel: Int = (1 - alpha) * 100 toInt

  /** Statistically rigorously compares means of samples using statistically rigorous evaluation method:
   *  <ul>
   *  <li>Confidence intervals for comparing 2 alternatives.
   *  <li>ANOVA for comparing 3 or more alternatives.
   *  </ul>
   *
   *  @param benchmark			The benchmark to be test
   *  @param mode					The benchmarking mode
   *  @param measurementResult	The just measured result
   *  @param history			The list of previous results
   *
   *  @return	The test result
   */
  def testDifference(benchmark: Benchmark,
                     mode: BenchmarkMode,
                     measurementResult: MeasurementSuccess,
                     history: History): RegressionResult = {
    if (history.length < 2) {
      throw new Exception("Not enough result files specified")
    }
    if (history.length == 2) {
      testConfidenceIntervals(benchmark, mode, measurementResult, history)
    }
    else {
      testANOVA(benchmark, mode, measurementResult, history)
    }
  }

  /** Statistically rigorously compares means of two samples using confidence intervals.
   *
   *  @param benchmark			The benchmark to be test
   *  @param mode					The benchmarking mode
   *  @param measurementResult	The just measured result
   *  @param history			The list of previous results
   *
   *  @return	The test result
   */
  private def testConfidenceIntervals(benchmark: Benchmark,
                                      mode: BenchmarkMode,
                                      measurementResult: MeasurementSuccess,
                                      history: History): RegressionResult = {
    var series = history.head

    val mean1 = mean(series)
    val s1 = standardDeviation(series)
    val n1 = series.length

    series = history.last

    val mean2 = mean(series)
    val s2 = standardDeviation(series)
    val n2 = series.length

    val diff = mean1 - mean2

    if (confidenceLevel == 100 && diff == 0) {
      RegressionSuccess(benchmark, mode, confidenceLevel, measurementResult)
    }
    else {
      reduceConfidenceLevel()

      val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)
      var c1: Double = 0
      var c2: Double = 0

      var ok = false

      while (isConfidenceLevelAcceptable && !ok) {
        if ((n1 >= 30) && (n2 >= 30)) {
          c1 = diff - inverseGaussianDistribution * s
          c2 = diff + inverseGaussianDistribution * s
        }
        else {
          var ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) /
            ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
          if (ndf == 0) {
            ndf = 1
          }
          c1 = diff - inverseStudentDistribution(ndf) * s
          c2 = diff + inverseStudentDistribution(ndf) * s
        }

        if ((c1 > 0 && c2 > 0) || (c1 < 0 && c2 < 0)) {
          reduceConfidenceLevel()
        }
        else {
          ok = true
        }
      }

      if (!ok) {
        ConfidenceIntervalFailure(
          benchmark,
          mode,
          confidenceLevel,
          measurementResult,
          ((mean1, standardDeviation(history.head)), (mean2, standardDeviation(history.last))),
          (c1, c2))
      }
      else {
        RegressionSuccess(benchmark, mode, confidenceLevel, measurementResult)
      }
    }
  }

  /** Statistically rigorously compares means of three or more samples using ANOVA.
   *
   *  @param benchmark			The benchmark to be test
   *  @param mode					The benchmarking mode
   *  @param measurementResult	The just measured result
   *  @param history			The list of previous results
   *
   *  @return	The test result
   */
  private def testANOVA(benchmark: Benchmark,
                        mode: BenchmarkMode,
                        measurementResult: MeasurementSuccess,
                        history: History): RegressionResult = {

    val sum = history.foldLeft(0: Long)((sum, p) => p.foldLeft(sum)((s, r) => s + r))

    val overall: Double = sum / (history.length * history.head.length)

    var SSA: Double = 0
    var SSE: Double = 0

    var means = ArrayBuffer[Double]()

    for (alternative <- history) {
      val alternativeMean = mean(alternative)
      means += alternativeMean
      SSA += (alternativeMean - overall) * (alternativeMean - overall) * alternative.length
      SSE += alternative.foldLeft(SSE) { (sse, a) => sse + (a - alternativeMean) * (a - alternativeMean) }
    }

    def meansAndSD: ArrayBuffer[(Double, Double)] =
      history map (alternative => (mean(alternative), standardDeviation(alternative)))

    if (confidenceLevel == 100 && SSE == 0) {
      // Memory case
      if (SSA != 0) {
        ANOVAFailure(
          benchmark,
          mode,
          confidenceLevel,
          measurementResult,
          meansAndSD,
          SSA,
          SSE,
          Double.PositiveInfinity,
          Double.NaN)
      }
      else {
        RegressionSuccess(benchmark, mode, confidenceLevel, measurementResult)
      }
    }
    else {
      // Performance case

      val n1 = history.length - 1
      val n2 = history.foldLeft(0)((s, p) => s + p.length) - history.length
      val FValue = SSA * n2 / SSE / n1
      var F: Double = 0

      var ok = false
      reduceConfidenceLevel()

      while (isConfidenceLevelAcceptable && !ok) {
        F = inverseFDistribution(n1, n2)

        log.debug("[SSA] " + SSA + "\t[SSE] " + SSE + "\t[FValue] " + FValue + "\t[F(" + n1 + ", " + n2 + ")] " + F)

        if (FValue <= F) {
          ok = true
        }
        else {
          reduceConfidenceLevel()
        }
      }

      if (ok) {
        RegressionSuccess(benchmark, mode, confidenceLevel, measurementResult)
      }
      else {
        ANOVAFailure(benchmark, mode, confidenceLevel, measurementResult, meansAndSD, SSA, SSE, FValue, F)
      }
    }
  }

}
