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
	 * The starting milestone of each iterator.
	 */
	protected var start: Long = 0
	/**
	 * The ending milestone of each iterator.
	 */
	protected var end: Long = 0
	/**
	 * The measured values series.
	 */
	protected var Series: List[Long] = List()
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

		val Mean = statistic.Mean
		val ConfidencInterval = statistic.ConfidenceInterval
		val diff = (ConfidencInterval.last - ConfidencInterval.head) / 2
		
		for (i <- Series) {
			println("[Sample]	" + i)
		}
		println("[Average]	" + Mean.formatted("%.2f"))
		println("[Confident Intervals]	[" + ConfidencInterval.head.formatted("%.2f") + "; " + ConfidencInterval.last.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + " = " + (diff / Mean * 100).formatted("%.2f") + "%")
	}

}
