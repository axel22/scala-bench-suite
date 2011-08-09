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

class Statistic(TIMESERES: List[Long]) {

	val alpha = 0.05
	val steadyThreshold = 0.02
	
	def constructConfidentInterval(runs: Int): List[Double] = {
		
		var diff: Double = 0

		if (runs >= 30) {
			diff = getGaussian(alpha) * StandardDeviation(runs) / sqrt(runs)
		} else {
			diff = getStudent(alpha) * StandardDeviation(runs) / sqrt(runs)
		}

		List(SampleMean(runs) - diff, SampleMean(runs) + diff)
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
	def getStudent(alpha: Double): Double = {
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
		var result = TIMESERES.head
		for (i <- TIMESERES) {
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
		var result = TIMESERES.head
		for (i <- TIMESERES) {
			if (result < i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return the average running time of repetitions
	 */
	def SampleMean(runs: Int): Double = {
		var sum: Double = 0
		for (i <- TIMESERES) {
			sum += i
		}
		sum / runs
	}
	
	/**
	 * @return the standard deviation of repetitions
	 */
	def StandardDeviation(runs: Int): Double = {
		var squareSum: Double = 0
		val sampleMean = SampleMean(runs)
		for (i <- TIMESERES) {
			squareSum += (i - sampleMean) * (i - sampleMean)
		}
		sqrt(squareSum / (runs - 1))
	}

	/**
	 * @return the coefficient of variation
	 */
	def CoV(runs: Int): Double = {
		StandardDeviation(runs) / SampleMean(runs)
	}
}