/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 09th 2011
 *
 * By ND P
 */

import scala.Math.sqrt

/**
 * Control the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
abstract class Harness {

	protected var timeStart: Long = 0
	protected var timeEnd: Long = 0
	protected var TimeSeries: List[Long] = List()
	protected var Mean: Double = 0

	protected var CILeft: Double = 0
	protected var CIRight: Double = 0
	
	val alpha = 0.05
	val steadyThreshold = 0.01

	/**
	 * Do the warm up and measure running time of the class snippet
	 */
	def run(): Unit = {
		println("Override this")
	}
	
	/**
	 * Calculate the result's Statistic
	 */
	def constructStatistic() {
		println("Override this")
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
	def MinTime(runningTimes: List[Long]): Long = {
		var result = runningTimes.head
		for (i <- runningTimes) {
			if (result > i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return the maximum running time
	 */
	def MaxTime(runningTimes: List[Long]): Long = {
		var result = runningTimes.head
		for (i <- runningTimes) {
			if (result < i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return the average running time of repetitions
	 */
	def ConstructMean(runs: Int): Double = {
		var sum: Double = 0
		for (i <- TimeSeries) {
			sum += i
		}
		Mean = sum / runs
		Mean
	}
	
	/**
	 * @return the lower bound of the confident interval
	 */
	def ConfidentIntervalLeft = CILeft
	
	/**
	 * @return the upper bound of the confident interval
	 */
	def ConfidentIntervalRight = CIRight

	/**
	 * @return the standard deviation of repetitions
	 */
	def StandardDeviation(runs: Int): Double = {
		var squareSum: Double = 0
		val sampleMean = ConstructMean(runs)
		for (i <- TimeSeries) {
			squareSum += (i - sampleMean) * (i - sampleMean)
		}
		sqrt(squareSum / (runs - 1))
	}

	/**
	 * @return the coefficient of variation
	 */
	def CoV(runs: Int): Double = {
		StandardDeviation(runs) / ConstructMean(runs)
	}
}