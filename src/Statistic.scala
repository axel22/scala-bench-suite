/*
 * Statistic
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

import scala.math.sqrt


/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
class Statistic(TIMESERIES: List[Long]) {

	/**
	 * The significant level.
	 */
	private val alpha = 0.01

	/**
	 * Computes the confidence interval for the given sample.
	 * 
	 * @return	The left and right end points of the confidence interval.
	 */
	def ConfidenceInterval(): List[Double] = {

		var diff: Double = 0
		val runs = TIMESERIES.length

		if (runs >= 30) {
			diff = inverseGaussianDistribution * StandardDeviation / sqrt(runs)
		} else {
			diff = inverseStudentDistribution(1) * StandardDeviation / sqrt(runs)
		}

		List(Mean - diff, Mean + diff)
	}

	/**
	 * Computes z value of the standard normal distribution with mean 0 and variance 1.
	 * 
	 * @param alpha	The significant level
	 * @return	The z value
	 */
	def inverseGaussianDistribution(): Double = {
		if (alpha == 0.10) {
			return 1.281551566
		} else if (alpha == 0.05) {
			return 1.644853627
		} else if (alpha == 0.01) {
			return 2.326347874
		} else {
			throw new Exception("Significant level are 0.10, 0.05 and 0.01 only")
		}
	}

	/**
	 * Loads the t value of the Student distribution from pre-computed table stored from file using:
	 * <ul>
	 * <li>A given degree of freedom
	 * <li>The pre-defined significant level
	 * </ul>
	 * 
	 * @param df	The degree of freedom
	 * @return	The t value
	 */
	def inverseStudentDistribution(df: Int): Double = {
		if (df > Utility.STUDENT_DISTRIBUTION_TABLE_ROW_MAX) {
			throw new Exception("Maximum degree of freedom is " + Utility.STUDENT_DISTRIBUTION_TABLE_ROW_MAX)
		}
		if (alpha == 0.10) {
			Utility.readCell(Utility.STUDENT_DISTRIBUTION_TABLE, Utility.STUDENT_DISTRIBUTION_TABLE_090, df)
		} else if (alpha == 0.05) {
			Utility.readCell(Utility.STUDENT_DISTRIBUTION_TABLE, Utility.STUDENT_DISTRIBUTION_TABLE_095, df)
		} else if (alpha == 0.01) {
			Utility.readCell(Utility.STUDENT_DISTRIBUTION_TABLE, Utility.STUDENT_DISTRIBUTION_TABLE_099, df)
		} else {
			throw new Exception("Significant level are 0.10, 0.05 and 0.01 only")
		}
	}

	/**
	 * Loads the F value of the Fisher F distribution from pre-computed table stored from file using:
	 * <ul>
	 * <li>Given degrees of freedom
	 * <li>The pre-defined significant level
	 * </ul>
	 * 
	 * @param n1	The first degree of freedom
	 * @param n2	The second degree of freedom
	 * @return	The F value
	 */
	def inverseFDistribution(n1: Int, n2: Int): Double = {
		if (n1 > Utility.F_DISTRIBUTION_TABLE_COLUMN_MAX) {
			throw new Exception("Maximum first degree of freedom is " + Utility.F_DISTRIBUTION_TABLE_COLUMN_MAX)
		}
		if (n2 > Utility.F_DISTRIBUTION_TABLE_ROW_MAX) {
			throw new Exception("Maximum second degree of freedom is " + Utility.F_DISTRIBUTION_TABLE_ROW_MAX)
		}
		if (alpha == 0.10) {
			Utility.readCell(Utility.F_DISTRIBUTION_TABLE_090, n1, n2)
		} else if (alpha == 0.05) {
			Utility.readCell(Utility.F_DISTRIBUTION_TABLE_095, n1, n2)
		} else if (alpha == 0.01) {
			Utility.readCell(Utility.F_DISTRIBUTION_TABLE_099, n1, n2)
		} else {
			throw new Exception("Significant level are 0.10, 0.05 and 0.01 only")
		}
	}

	/**
	 * @return	The minimum running time
	 */
	def MinTime(): Long = {
		var result = TIMESERIES.head
		for (i <- TIMESERIES) {
			if (result > i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return	The maximum running time
	 */
	def MaxTime(): Long = {
		var result = TIMESERIES.head
		for (i <- TIMESERIES) {
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
		val runs = TIMESERIES.length
		for (i <- TIMESERIES) {
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
		val runs = TIMESERIES.length
		val mean = Mean
		for (i <- TIMESERIES) {
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
}