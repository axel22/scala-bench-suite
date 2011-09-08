/*
 * Statistic
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.math.sqrt

import org.apache.commons.math.distribution.FDistributionImpl
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl

import ndp.scala.tools.sbs.measurement.BenchmarkResult
import ndp.scala.tools.sbs.util.Log

/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
object Statistic {

  /**
   * Minimum significant level.
   */
  private val alphaMax: Double = 0.2
  
  /**
   * Maximum significant level.
   */
  private val alphaMin: Double = 0
  
  /**
   * The significant level.
   */
  private var alpha = alphaMin
  
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
      true
    }
    else if (alpha == 0.01) {
      alpha = 0.05
      true
    }
    else if (alpha <= alphaMax) {
      alpha += 0.05
      true
    }
    else {
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
  def confidenceInterval(series: BenchmarkResult): (Double, Double) = {

    var diff: Double = 0

    if (series.length >= 30) {
      diff = inverseGaussianDistribution * standardDeviation(series) / sqrt(series.length)
    } else {
      diff = inverseStudentDistribution(series.length - 1) * standardDeviation(series) / sqrt(series.length)
    }

    (mean(series) - diff, mean(series) + diff)
  }

  /**
   * Computes z value of the standard normal distribution with mean 0 and variance 1.
   *
   * @param alpha	The significant level
   * @return	The z value
   */
  private def inverseGaussianDistribution(): Double = {
    new NormalDistributionImpl(0, 1).inverseCumulativeProbability(1 - alpha / 2)
  }

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
  private def inverseStudentDistribution(df: Int): Double = {
    new TDistributionImpl(df).inverseCumulativeProbability(1 - alpha / 2)
  }

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
  private def inverseFDistribution(n1: Int, n2: Int): Double = {
    new FDistributionImpl(n1, n2).inverseCumulativeProbability(1 - alpha)
  }

  /**
   * @param series	The result of benchmarking
   * @return	The minimum value
   */
  def min(series: BenchmarkResult): Long = {
    var result = series.head
    for (i <- series) {
      if (result > i) {
        result = i
      }
    }
    result
  }

  /**
   * @param series	The result of benchmarking
   * @return	The maximum value
   */
  def max(series: BenchmarkResult): Long = {
    var result = series.head
    for (i <- series) {
      if (result < i) {
        result = i
      }
    }
    result
  }

  /**
   * Computes the sample mean.
   *
   * @param series	The result of benchmarking
   * @return	The average
   */
  def mean(series: BenchmarkResult): Double = {
    var sum: Double = 0
    val runs = series.length
    for (i <- series) {
      sum += i
    }
    sum / runs
  }

  /**
   * Computes the standard deviation of a given sample.
   *
   * @param series	The result of benchmarking
   * @return	The standard deviation
   */
  def standardDeviation(series: BenchmarkResult): Double = {
    var squareSum: Double = 0
    val runs = series.length
    val mean: Double = this.mean(series)
    for (i <- series) {
      squareSum += (i - mean) * (i - mean)
    }
    sqrt(squareSum / (runs - 1))
  }

  /**
   * Computes the coefficient of variation of a given sample.
   *
   * @param series	The result of benchmarking
   * @return	The coefficient of variation
   */
  def CoV(series: BenchmarkResult): Double = {
    standardDeviation(series) / mean(series)
  }

  /**
   * @return	The significant level alpha
   */
  def significantLevel = alpha

  /**
   * @return	The confident level
   */
  def confidentLevel = (1 - alpha) * 100

  /**
   * Statistically rigorously compares means of samples using statistically rigorous evaluation method:
   * <ul>
   * <li>Confidence intervals for comparing 2 alternatives.
   * <li>ANOVA for comparing 3 or more alternatives.
   * </ul>
   *
   * @param persistor	The list of previous results
   * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
   */
  def testDifference(persistor: Persistor): Either[Boolean, String] = {
    if (persistor.length < 2) {
      Right("Not enough result files specified. No regression.")
    }
    if (persistor.length == 2) {
      Left(testConfidenceIntervals(persistor))
    } else {
      Left(testANOVA(persistor))
    }
  }

  /**
   * Statistically rigorously compares means of two samples using confidence intervals.
   *
   * @param persistor	The list of previous results
   * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
   */
  private def testConfidenceIntervals(persistor: Persistor): Boolean = {
    var series = persistor(0)

    val mean1 = mean(series)
    val s1 = standardDeviation(series)
    val n1 = series.length

    series = persistor(1)

    val mean2 = mean(series)
    val s2 = standardDeviation(series)
    val n2 = series.length

    val diff = mean1 - mean2
    val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)

    var c1: Double = 0
    var c2: Double = 0

    if ((n1 >= 30) && (n2 >= 30)) {
      c1 = diff - inverseGaussianDistribution * s
      c2 = diff + inverseGaussianDistribution * s
    } else {
      val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
      c1 = diff - inverseStudentDistribution(ndf) * s
      c2 = diff + inverseStudentDistribution(ndf) * s
    }

    if (((c1 > 0) && (c2 > 0)) || ((c1 < 0) && (c2 < 0))) {
      true
    } else {
      false
    }
  }

  /**
   * Statistically rigorously compares means of three or more samples using ANOVA.
   *
   * @param persistor	The list of previous results
   * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
   */
  private def testANOVA(persistor: Persistor): Boolean = {
    var sum: Long = 0

    for (alternative <- persistor) {
      for (invidual <- alternative) {
        sum += invidual
      }
    }

    val overall: Double = sum / (persistor.length * persistor.head.length)

    var SSA: Double = 0
    var SSE: Double = 0
    for (alternative <- persistor) {
      val alternativeMean = mean(alternative)
      SSA += (alternativeMean - overall) * (alternativeMean - overall)

      for (invidual <- alternative) {
        SSE += (invidual - alternativeMean) * (invidual - alternativeMean)
      }
    }
    SSA *= persistor.head.length

    val n1 = persistor.length - 1
    val n2 = persistor.length * persistor.head.length - persistor.length
    val FValue: Double = SSA * n2 / SSE / n1
    
    log.debug("[SSA] " + SSA + "\t[SSE] " + SSE + "\t[FValue] " + FValue + "\t[F(" + n1 + ", " + n2 + ")] " + inverseFDistribution(n1, n2))

    if (FValue > inverseFDistribution(n1, n2)) true else false
  }

}
