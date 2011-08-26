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

import scala.math.sqrt
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl
import org.apache.commons.math.distribution.FDistributionImpl
import scala.collection.mutable.ArrayBuffer

/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
class Statistic() {

	/**
	 * The significant level.
	 */
	private var alpha = 0.01
	/**
	 * The sample value series.
	 */
	private var _series: ArrayBuffer[Long] = null
	def series = _series
	def series_=(series: ArrayBuffer[Long]) {
	  _series = series
	}
	/**
	 * The <code>persistors</code> of sample value series.
	 */
	private var _persistors: ArrayBuffer[ArrayBuffer[Long]] = null
	def persistors = _persistors
	def persistors_=(persistors: ArrayBuffer[ArrayBuffer[Long]]) {
	  _persistors = persistors
	}

	/**
	 * Constructs a <code>Statistic</code> using a given persistors of samples.
	 * 
	 * @param thepersistors	The given persistors of samples
	 */
	/*def this(thepersistors: persistors[persistors[Long]]) {
		this
		persistors = thepersistors
	}*/
	
	/**
	 * Constructs a <code>Statistic</code> using a given sample.
	 * 
	 * @param theseries	The given sample
	 */
	def this(theSeries: ArrayBuffer[Long]) {
		this
		series = theSeries
	}
	
	/**
	 * Computes the confidence interval for the given sample.
	 *
	 * @return	The left and right end points of the confidence interval.
	 */
	def ConfidenceInterval(): ArrayBuffer[Double] = {

		var diff: Double = 0

		if (series.length >= 30) {
			diff = inverseGaussianDistribution * standardDeviation / sqrt(series.length)
		} else {
			diff = inverseStudentDistribution(series.length - 1) * standardDeviation / sqrt(series.length)
		}

		ArrayBuffer(mean - diff, mean + diff)
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
		new TDistributionImpl(series.length - 1).inverseCumulativeProbability(1 - alpha / 2)
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
	 * @return	The minimum value
	 */
	def min(): Long = {
		var result = series.head
		for (i <- series) {
			if (result > i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return	The maximum value
	 */
	def max(): Long = {
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
	 * @return	The average
	 */
	def mean(): Double = {
		var sum: Double = 0
		val runs = series.length
		for (i <- series) {
			sum += i
		}
		sum / runs
	}

	/**
	 * Computes the standard deviation of a given sample.
	 * @return	The standard deviation
	 */
	def standardDeviation(): Double = {
		var squareSum: Double = 0
		val runs = series.length
		for (i <- series) {
			squareSum += (i - mean) * (i - mean)
		}
		sqrt(squareSum / (runs - 1))
	}

	/**
	 * Computes the coefficient of variation of a given sample.
	 * @return	The coefficient of variation
	 */
	def CoV(): Double = {
		standardDeviation / mean
	}

	/**
	 * @return	The significant level alpha
	 */
	def SignificantLevel = alpha

	/**
	 * @return	The confident level
	 */
	def ConfidentLevel = (1 - alpha) * 100
	
	/**
	 * Statistically rigorously compares means of samples using statistically rigorous evaluation method:
	 * <ul>
	 * <li>Confidence intervals for comparing 2 alternatives.
	 * <li>ANOVA for comparing 3 or more alternatives.
	 * </ul>
	 * 
	 * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
	 */
	def testDifference(): Boolean = {
		if (persistors.length < 2) {
			throw new java.lang.Exception("Not enough result files specified. No regression.")
		}
		if (persistors.length == 2) {
			testConfidenceIntervals
		} else {
			testANOVA
		}
	}
	
	/**
	 * Statistically rigorously compares means of two samples using confidence intervals.
	 * 
	 * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
	 */
	private def testConfidenceIntervals(): Boolean = {
		series = persistors(0)

		val mean1 = mean
		val s1 = standardDeviation
		val n1 = series.length

		series = persistors(1)

		val mean2 = mean
		val s2 = standardDeviation
		val n2 = series.length

		val diff = mean1 - mean2
		val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)

		var c1: Double = 0
		var c2: Double = 0

		if ((n1 >= 30) && (n2 >= 30)) {
			c1 = diff - inverseGaussianDistribution * s
			c2 = diff + inverseGaussianDistribution * s
		}
		else {
			val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
			c1 = diff - inverseStudentDistribution(ndf) * s
			c2 = diff + inverseStudentDistribution(ndf) * s
		}
		
		println("[Difference] " + diff + "\t[Standard Deviation] " + s)
		println("[Confidence Interval] [" + c1 + "; " + c2 + "]")

		if (((c1 > 0) && (c2 > 0)) || ((c1 < 0) && (c2 < 0))) {
			true
		}
		else {
			false
		}
	}
	
	/**
	 * Statistically rigorously compares means of three or more samples using ANOVA.
	 * 
	 * @return	<code>true</code> if there is statistically significant difference among the means, <code>false</code> otherwise
	 */
	private def testANOVA(): Boolean = {
		var sum: Long = 0
		for (alternative <- persistors) {
			for (invidual <- alternative) {
				sum += invidual
			}
		}
		val overall = sum / (persistors.length * persistors.head.length)

		var SSA: Double = 0
		var SSE: Double = 0
		for (alternative <- persistors) {
			series = alternative
			val alternativeMean = mean
			SSA += (alternativeMean - overall) * (alternativeMean - overall)

			for (invidual <- alternative) {
				SSE += (invidual - alternativeMean) * (invidual - alternativeMean)
			}
		}
		SSA *= series.length
		
		val n1 = persistors.length - 1
		val n2 = persistors.length * series.length - persistors.length
		val FValue: Double = SSA * n2 / SSE / n1
		println("[SSA] " + SSA + "\t[SSE] " + SSE + "\t[FValue] " + FValue + "\t[F(" + n1 + ", " + n2 + ")] " + inverseFDistribution(n1, n2))
		
		if (FValue > inverseFDistribution(n1, n2)) {
			true
		}
		else {
			false
		}
	}

}
