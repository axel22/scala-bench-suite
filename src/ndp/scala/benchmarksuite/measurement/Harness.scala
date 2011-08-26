/*
 * Harness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import java.lang.Thread.sleep

import scala.compat.Platform

import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.BenchmarkResult

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
	 * Does the warm up and measure metric of the benchmark classes.
	 */
	def run() {
		println("Override this")
	}

	/**
	 * Calculates the result's statistic metrics.
	 */
	def constructStatistic() {

		statistic = new Statistic(Series)
		
		val Mean = statistic.Mean
		val ConfidenceInterval = statistic.ConfidenceInterval
		val diff = (ConfidenceInterval.last - ConfidenceInterval.head) / 2

		for (i <- Series) {
			println("[Sample]	" + i)
		}
		println("[Average]	" + Mean.formatted("%.2f"))
		println("[Confident Intervals]	[" + ConfidenceInterval.head.formatted("%.2f") + "; " + ConfidenceInterval.last.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + " = " + (diff / Mean * 100).formatted("%.2f") + "%")
	}

	/**
	 * Forces the Java gc to clean up the heap.
	 */
	def cleanUp() {
		Platform.collectGarbage
		System.runFinalization
		sleep(100)
		Platform.collectGarbage
	}
}
