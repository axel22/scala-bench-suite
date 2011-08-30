/*
 * Statistic
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.regression

import scala.collection.mutable.ArrayBuffer
import scala.math.sqrt

import org.apache.commons.math.distribution.FDistributionImpl
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl

import ndp.scala.benchmarksuite.utility.Log

/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
object Statistic {

  /**
   * The significant level.
   */
  private var alpha = 0.01
  /*/**
   * The sample value series.
   */
  private var _series: ArrayBuffer[Long] = null
  def series = _series
  def series_=(series: ArrayBuffer[Long]) {
    _series = series
  }
  /**
   * The array of benchmark result.
   */
  private var _persistor: Persistor = null
  def persistor = _persistor
  def persistor_=(persistor: Persistor) {
    _persistor = persistor
  }

  /**
   * Constructs a <code>Statistic</code> using a given array of benchmark result.
   *
   * @param log	The logger
   * @param config
   * @param thepersistors	The given array of benchmark result
   */
  def this(log: Log, config: Config, thePersistor: Persistor) {
    this(log, config)
    persistor = thePersistor
  }

  /**
   * Constructs a <code>Statistic</code> using a given sample.
   *
   * @param log	The logger
   * @param config
   * @param theseries	The given sample
   */
  def this(log: Log, config: Config, theSeries: BenchmarkResult) {
    this(log, config)
    series = theSeries
  }*/

  /**
   * Computes the confidence interval for the given sample.
   *
   * @param series	The result of benchmarking
   * @return	The left and right end points of the confidence interval.
   */
  def confidenceInterval(series: BenchmarkResult): ArrayBuffer[Double] = {

    var diff: Double = 0

    if (series.length >= 30) {
      diff = inverseGaussianDistribution * standardDeviation(series) / sqrt(series.length)
    } else {
      diff = inverseStudentDistribution(series.length - 1) * standardDeviation(series) / sqrt(series.length)
    }

    ArrayBuffer(mean(series) - diff, mean(series) + diff)
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
  def testDifference(persistor: Persistor): Boolean = {
    if (persistor.length < 2) {
      throw new java.lang.Exception("Not enough result files specified. No regression.")
    }
    if (persistor.length == 2) {
      testConfidenceIntervals(persistor)
    } else {
      testANOVA(persistor)
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
    val log = new Log

    for (alternative <- persistor) {
      for (invidual <- alternative) {
        sum += invidual
      }
    }

    val overall: Double = sum / (persistor.length * persistor.head.length)

    log debug "[Overall] " + overall

    var SSA: Double = 0
    var SSE: Double = 0
    for (alternative <- persistor) {
      val alternativeMean = mean(alternative)
      log debug "[Alternative mean]	" + alternativeMean
      SSA += (alternativeMean - overall) * (alternativeMean - overall)

      for (invidual <- alternative) {
        SSE += (invidual - alternativeMean) * (invidual - alternativeMean)
      }
    }
    SSA *= persistor.head.length

    val n1 = persistor.length - 1
    val n2 = persistor.length * persistor.head.length - persistor.length
    val FValue: Double = SSA * n2 / SSE / n1
    log debug ("[SSA] " + SSA + "\t[SSE] " + SSE + "\t[FValue] " + FValue + "\t[F(" + n1 + ", " + n2 + ")] " + inverseFDistribution(n1, n2))

    if (FValue > inverseFDistribution(n1, n2)) true else false
  }

}
