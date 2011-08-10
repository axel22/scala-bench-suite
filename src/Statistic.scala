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

	val alpha = 0.05
	val steadyThreshold = 0.02
	
	def ConfidentInterval(): List[Double] = {
		
		var diff: Double = 0
		val runs = TIMESERIES.length

		if (runs >= 30) {
			diff = getGaussian(alpha) * StandardDeviation / sqrt(runs)
		} else {
			diff = getStudent(alpha, 1) * StandardDeviation / sqrt(runs)
		}

		List(Mean - diff, Mean + diff)
	}
	
	/**
	 * Function getGaussian
	 * @param alpha: the significant level
	 */
	def getGaussian(alpha: Double): Double = {
		if (alpha == 0.05) {
			1.96
		} else {
			1
		}
	}

	/**
	 * Function getStudent
	 * @param alpha: the significant level
	 */
	def getStudent(alpha: Double, n: Int): Double = {
		if (alpha == 0.05) {
			1.796
		} else {
			1
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