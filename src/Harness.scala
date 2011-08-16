/*
 * Harness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

import scala.Math.sqrt


/**
 * Abstract base class for iterating and measuring the running time of the benchmark classes.
 *
 * @author ND P
 */
abstract class Harness {

	/**
	 * The starting time of each iterator.
	 */
	protected var timeStart: Long = 0
	/**
	 * The ending time of each iterator.
	 */
	protected var timeEnd: Long = 0
	/**
	 * The measured running time series.
	 */
	protected var TimeSeries: List[Long] = List()
	/**
	 * The <code>Statistic</code> class used for computing statistic arguments.
	 */
	protected var statistic: Statistic = null
	/**
	 * The <code>BenchmarkResult</code> class representing the benchmarking result.
	 */
	protected var result: BenchmarkResult = null

	/**
	 * Does the warm up and measure running time of the benchmark classes.
	 */
	def run() {
		println("Override this")
	}

	/**
	 * Calculates the result's statistic arguments.
	 */
	def constructStatistic() {
		println("Override this")
	}

}