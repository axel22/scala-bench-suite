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
	protected var statistic: Statistic = null
	
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

}