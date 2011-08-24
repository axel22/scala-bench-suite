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
	private var SERIES: List[Long] = Nil
	/**
	 * The <code>List</code> of sample value series.
	 */
	private var LIST: List[List[Long]] = Nil

	/**
	 * Constructs a <code>Statistic</code> using a given list of samples.
	 * 
	 * @param theList	The given list of samples
	 */
	/*def this(theList: List[List[Long]]) {
		this
		LIST = theList
	}*/
	
	/**
	 * Constructs a <code>Statistic</code> using a given sample.
	 * 
	 * @param theSeries	The given sample
	 */
	def this(theSeries: List[Long]) {
		this
		SERIES = theSeries
	}
	
	/**
	 * Sets the field <code>SERIES</code> with the new value.
	 *
	 * @param newSeries	The new value series
	 */
	def setSERIES(newSeries: List[Long]) {
		SERIES = newSeries
	}
	
	/**
	 * Sets the field <code>LIST</code> with the new value.
	 *
	 * @param newList	The new list of samples.
	 */
	def setLIST(newList: List[List[Long]]) {
		LIST = newList
	}

	/**
	 * Computes the confidence interval for the given sample.
	 *
	 * @return	The left and right end points of the confidence interval.
	 */
	def ConfidenceInterval(): List[Double] = {

		var diff: Double = 0

		if (SERIES.length >= 30) {
			diff = inverseGaussianDistribution * StandardDeviation / sqrt(SERIES.length)
		} else {
			diff = inverseStudentDistribution(SERIES.length - 1) * StandardDeviation / sqrt(SERIES.length)
		}

		List(Mean - diff, Mean + diff)
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
		new TDistributionImpl(SERIES.length - 1).inverseCumulativeProbability(1 - alpha / 2)
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
		var result = SERIES.head
		for (i <- SERIES) {
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
		var result = SERIES.head
		for (i <- SERIES) {
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
	def Mean(): Double = {
		var sum: Double = 0
		val runs = SERIES.length
		for (i <- SERIES) {
			sum += i
		}
		sum / runs
	}

	/**
	 * Computes the standard deviation of a given sample.
	 * @return	The standard deviation
	 */
	def StandardDeviation(): Double = {
		var squareSum: Double = 0
		val runs = SERIES.length
		val mean = Mean
		for (i <- SERIES) {
			squareSum += (i - mean) * (i - mean)
		}
		sqrt(squareSum / (runs - 1))
	}

	/**
	 * Computes the coefficient of variation of a given sample.
	 * @return	The coefficient of variation
	 */
	def CoV(): Double = {
		StandardDeviation / Mean
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
		if (LIST.length < 2) {
			throw new java.lang.Exception("Not enough result files specified. No regression.")
		}
		if (LIST.length == 2) {
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
		setSERIES(LIST.head)

		val mean1 = Mean
		val s1 = StandardDeviation
		val n1 = SERIES.length

		setSERIES(LIST.last)

		val mean2 = Mean
		val s2 = StandardDeviation
		val n2 = SERIES.length

		val mean = mean1 - mean2
		val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)

		var c1: Double = 0
		var c2: Double = 0

		if ((n1 >= 30) && (n2 >= 30)) {
			c1 = mean - inverseGaussianDistribution * s
			c2 = mean + inverseGaussianDistribution * s
		}
		else {
			val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
			c1 = mean - inverseStudentDistribution(ndf) * s
			c2 = mean + inverseStudentDistribution(ndf) * s
		}
		
		println("[Mean] " + mean + "\t[Standard Deviation] " + s)
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
		for (alternative <- LIST) {
			for (invidual <- alternative) {
				sum += invidual
			}
		}
		val overall = sum / (LIST.length * LIST.head.length)

		var SSA: Double = 0
		var SSE: Double = 0
		for (alternative <- LIST) {
			setSERIES(alternative)
			val alternativeMean = Mean
			SSA += (alternativeMean - overall) * (alternativeMean - overall)

			for (invidual <- alternative) {
				SSE += (invidual - alternativeMean) * (invidual - alternativeMean)
			}
		}
		SSA *= SERIES.length
		
		val n1 = LIST.length - 1
		val n2 = LIST.length * SERIES.length - LIST.length
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
