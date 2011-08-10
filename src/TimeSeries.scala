/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 10th 2011
 *
 * By ND P
 */

import scala.math.sqrt

class TimeSeries {
	
	private var series: List[Long] = Nil
	private var alpha: Double = 0.05
	
	def this(TIMESERIES: List[Long]) {
		this
		series = TIMESERIES
	}
	
	def :: (ele: Long): TimeSeries = {
		series ::= ele
		this
	}
	
	def ++ (ele: Long) {
		series = series ++ List(ele)
	}
	
	def head: TimeSeries = {
		new TimeSeries(List(series.head))
	}
	
	def tail(): TimeSeries = {
		new TimeSeries(series.tail)
	}
	
	def ConfidentInterval(): List[Double] = {
		
		var diff: Double = 0
		val runs = series.length

		if (runs >= 30) {
			diff = getGaussian(alpha) * StandardDeviation() / sqrt(runs)
		} else {
			diff = getStudent(alpha) * StandardDeviation() / sqrt(runs)
		}

		List(Mean() - diff, Mean() + diff)
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
		var result = series.head
		for (i <- series) {
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
		var result = series.head
		for (i <- series) {
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
		val runs = series.length
		for (i <- series) {
			sum += i
		}
		sum / runs
	}
	
	/**
	 * @return the standard deviation of repetitions
	 */
	def StandardDeviation(): Double = {
		var squareSum: Double = 0
		val runs = series.length
		val mean = Mean()
		for (i <- series) {
			squareSum += (i - mean) * (i - mean)
		}
		sqrt(squareSum / (runs - 1))
	}

	/**
	 * @return the coefficient of variation
	 */
	def CoV(): Double = {
		StandardDeviation() / Mean()
	}

}