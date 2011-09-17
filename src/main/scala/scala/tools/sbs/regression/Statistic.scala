/*
 * Statistic
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.math.sqrt
import org.apache.commons.math.distribution.FDistributionImpl
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
class Statistic(log: Log, config: Config, var alpha: Double = 0) {

  /**
   * Minimum significant level.
   */
  private val alphaMax: Double = 0.1

  /**
   * Maximum significant level.
   */
  private val alphaMin: Double = 0.00

  /**
   * Reduces the confidence level time by time to by 5% each time,
   * except 2 cases:
   * <ul>
   * <li>From 100% to 99%
   * <li>From 99% to 95%
   * </ul>
   *
   * @return `true` if success, `false` if the confidence leval is at the minimum value.
   */
  def reduceConfidenceLevel(): Boolean = {
    if (alpha == 0) {
      alpha = 0.01
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
      true
    } else if (alpha == 0.01) {
      alpha = 0.05
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
      true
    } else if (alpha <= alphaMax) {
      alpha += 0.05
      log.verbose("Confidence level was reduced to " + confidenceLevel + "%")
      true
    } else {
      log.verbose("Confidence level is not reducible")
      false
    }
  }

  /**
   * @return `true` if the confidence level is GE `1 - MAX_ALPHA`, `false` otherwise
   */
  def isConfidenceLevelAcceptable = if (alpha <= alphaMax) true else false

  def resetConfidenceInterval() {
    alpha = alphaMin
  }

  /**
   * Computes the confidence interval for the given sample.
   *
   * @param series	The result of benchmarking
   * @return	The left and right end points of the confidence interval.
   */
  def confidenceInterval(series: Series): (Double, Double) = {
    val SD = standardDeviation(series)
    var diff: Double = 0

    if (SD == 0) {
      diff = 0
    } else if (series.length >= 30) {
      diff = inverseGaussianDistribution * SD / sqrt(series.length)
    } else {
      diff = inverseStudentDistribution(series.length - 1) * SD / sqrt(series.length)
    }
    (mean(series) - diff, mean(series) + diff)
  }

  /**
   * Computes z value of the standard normal distribution with mean 0 and variance 1.
   *
   * @param alpha	The significant level
   * @return	The z value
   */
  private def inverseGaussianDistribution() =
    new NormalDistributionImpl(0, 1).inverseCumulativeProbability(1 - alpha / 2)

  /**
   * Computes the t value of the Student distribution using:
   * <ul>
   * <li>A given degree of freedom
   * <li>The pre-defined significant level
   * </ul>
   *
   * @param df	The degree of freedom
   * @return	The t value
   */
  private def inverseStudentDistribution(df: Int) =
    new TDistributionImpl(df).inverseCumulativeProbability(1 - alpha / 2)

  /**
   * Computes the F value of the Fisher F distribution using:
   * <ul>
   * <li>Given degrees of freedom
   * <li>The pre-defined significant level
   * </ul>
   *
   * @param n1	The first degree of freedom
   * @param n2	The second degree of freedom
   * @return	The F value
   */
  private def inverseFDistribution(n1: Int, n2: Int) =
    new FDistributionImpl(n1, n2).inverseCumulativeProbability(1 - alpha)

  /**
   * @param series	The result of benchmarking
   * @return	The minimum value
   */
  def min(series: Series) = series.foldLeft(series.head) { (min, s) => if (min < s) min else s }

  /**
   * @param series	The result of benchmarking
   * @return	The maximum value
   */
  def max(series: Series) = series.foldRight(series.last) { (max, s) => if (max > s) max else s }

  /**
   * Computes the sample mean.
   *
   * @param series	The result of benchmarking
   * @return	The average
   */
  def mean(series: Series) = series.foldLeft(0: Double) { (sum: Double, s) => sum + s } / series.length

  /**
   * Computes the standard deviation of a given sample.
   *
   * @param series	The result of benchmarking
   * @return	The standard deviation
   */
  def standardDeviation(series: Series): Double = {
    val mean = this.mean(series)
    sqrt(series.foldLeft(0: Double) { (squareSum, s) => squareSum + (s - mean) * (s - mean) } / (series.length - 1))
  }

  /**
   * Computes the coefficient of variation of a given sample.
   *
   * @param series	The result of benchmarking
   * @return	The coefficient of variation
   */
  def CoV(series: Series) = standardDeviation(series) / mean(series)

  /**
   * @return	The significant level alpha
   */
  def significantLevel = alpha

  /**
   * @return	The confident level
   */
  def confidenceLevel = (1 - alpha) * 100

  /**
   * Statistically rigorously compares means of samples using statistically rigorous evaluation method:
   * <ul>
   * <li>Confidence intervals for comparing 2 alternatives.
   * <li>ANOVA for comparing 3 or more alternatives.
   * </ul>
   *
   * @param persistor	The list of previous results
   * @return	`true` if there is statistically significant difference among the means, `false` otherwise
   */
  def testDifference(persistor: Persistor): Either[Option[(Double, Double)], Option[ArrayBuffer[Double]]] = {
    if (persistor.length < 2) {
      throw new Exception("Not enough result files specified at " + persistor.location.path)
    }
    if (persistor.length == 2) {
      Left(testConfidenceIntervals(persistor))
    } else {
      Right(testANOVA(persistor))
    }
  }

  /**
   * Statistically rigorously compares means of two samples using confidence intervals.
   *
   * @param persistor	The list of previous results
   * @return	The confidence interval if there is statistically significant difference, `None` otherwise
   */
  private def testConfidenceIntervals(persistor: Persistor): Option[(Double, Double)] = {
    var series = persistor(0).series

    val mean1 = mean(series)
    val s1 = standardDeviation(series)
    val n1 = series.length

    series = persistor(1).series

    val mean2 = mean(series)
    val s2 = standardDeviation(series)
    val n2 = series.length

    val diff = mean1 - mean2
    val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)

    var c1: Double = 0
    var c2: Double = 0

    if (confidenceLevel == 100 && diff == 0) {
      None
    } else {
      reduceConfidenceLevel()
      if ((n1 >= 30) && (n2 >= 30)) {
        c1 = diff - inverseGaussianDistribution * s
        c2 = diff + inverseGaussianDistribution * s
      } else {
        var ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) /
          ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
        if (ndf == 0) {
          ndf = 1
        }
        c1 = diff - inverseStudentDistribution(ndf) * s
        c2 = diff + inverseStudentDistribution(ndf) * s
      }

      if ((c1 > 0 && c2 > 0) || (c1 < 0 && c2 < 0)) Some(c1, c2) else None
    }
  }

  /**
   * Statistically rigorously compares means of three or more samples using ANOVA.
   *
   * @param persistor	The list of previous results
   * @return	Array of the means if there is statistically significant difference, `None` otherwise
   */
  private def testANOVA(persistor: Persistor): Option[ArrayBuffer[Double]] = {

    val sum = persistor.foldLeft(0: Long)((sum, p) => p.series.foldLeft(sum)((s, r) => s + r))

    val overall: Double = sum / (persistor.length * persistor.head.series.length)

    var SSA: Double = 0
    var SSE: Double = 0

    for (alternative <- persistor) {
      val alternativeMean = mean(alternative.series)
      SSA += (alternativeMean - overall) * (alternativeMean - overall) * alternative.series.length
      SSE += alternative.series.foldLeft(SSE) { (sse, a) => sse + (a - alternativeMean) * (a - alternativeMean) }
    }

    if (confidenceLevel == 100 && SSE == 0 && SSA != 0) {
      // Memory case
      Some(persistor.foldLeft(new ArrayBuffer[Double]) { (s, p) => s + mean(p.series) })
    } else if (SSE == 0 && SSA == 0) {
      None
    } else {
      // Performance case
      reduceConfidenceLevel()
      val n1 = persistor.length - 1
      val n2 = persistor.foldLeft(0)((s, p) => s + p.series.length) - persistor.length
      val FValue = SSA * n2 / SSE / n1

      log.debug(
        "[SSA] " + SSA +
          "\t[SSE] " + SSE +
          "\t[FValue] " + FValue +
          "\t[F(" + n1 + ", " + n2 + ")] " + inverseFDistribution(n1, n2))

      if (FValue <= inverseFDistribution(n1, n2)) {
        None
      } else {
        Some(persistor.foldLeft(new ArrayBuffer[Double]) { (s, p) => s + mean(p.series) })
      }
    }
  }

}