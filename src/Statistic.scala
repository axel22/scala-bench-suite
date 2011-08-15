/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 09th 2011
 *
 * By ND P
 */

import scala.math.sqrt

class Statistic(TIMESERIES: List[Long]) {

	private val alpha = 0.05
	private val steadyThreshold = 0.02

	def ConfidentInterval(): List[Double] = {

		var diff: Double = 0
		val runs = TIMESERIES.length

		if (runs >= 30) {
			diff = inverseGaussianDistribution(alpha) * StandardDeviation / sqrt(runs)
		} else {
			diff = inverseStudentDistribution(alpha, 1) * StandardDeviation / sqrt(runs)
		}

		List(Mean - diff, Mean + diff)
	}

	/**
	 * Function inverseGaussianDistribution
	 * Compute z value of the standard normal distribution with mean 0 and variance 1
	 * @param alpha: the significant level
	 * @return: the z value
	 */
	def inverseGaussianDistribution(alpha: Double): Double = {
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
	 * Function inverseStudentDistribution
	 * Compute t value of the Student distribution
	 * @param alpha: the significant level
	 * @param n: the degree of freedom
	 * @return the t value
	 */
	def inverseStudentDistribution(alpha: Double, df: Int): Double = {
		if (df > Utility.StudentDistTblMAXROW) {
			throw new Exception("Maximum degree of freedom is " + Utility.StudentDistTblMAXROW)
		}
		if (alpha == 0.10) {
			Utility.readCell(Utility.StudentDistTable, Utility.StudentDistTblClmn090, df)
		} else if (alpha == 0.05) {
			Utility.readCell(Utility.StudentDistTable, Utility.StudentDistTblClmn095, df)
		} else if (alpha == 0.01) {
			Utility.readCell(Utility.StudentDistTable, Utility.StudentDistTblClmn099, df)
		} else {
			throw new Exception("Significant level are 0.10, 0.05 and 0.01 only")
		}
	}
	
	/**
	 * Function inverseFDistribution
	 * Compute F value of the Fisher F distribution
	 * @param alpha: the significant level
	 * @param n1: the first degree of freedom
	 * @param n2: the second degree of freedom
	 * @return the F value
	 */
	def inverseFDistribution(alpha: Double, n1: Int, n2: Int): Double = {
		if (n1 > Utility.FDistTblMAXCOLUMN) {
			throw new Exception("Maximum first degree of freedom is " + Utility.FDistTblMAXCOLUMN)
		}
		if (n2 > Utility.FDistTblMAXROW) {
			throw new Exception("Maximum second degree of freedom is " + Utility.FDistTblMAXROW)
		}
		if (alpha == 0.10) {
			Utility.readCell(Utility.FDistTable090, n1, n2)
		} else if (alpha == 0.05) {
			Utility.readCell(Utility.FDistTable095, n1, n2)
		} else if (alpha == 0.01) {
			Utility.readCell(Utility.FDistTable099, n1, n2)
		} else {
			throw new Exception("Significant level are 0.10, 0.05 and 0.01 only")
		}
	}

	/**
	 * @return the minimum running time
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
	 * @return the maximum running time
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
	 * @return the average running time of repetitions
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
	 * @return the standard deviation of repetitions
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
	 * @return the coefficient of variation
	 */
	def CoV(): Double = {
		StandardDeviation / Mean
	}
}