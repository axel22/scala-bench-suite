/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 09th 2011
 *
 * By ND P
 */

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

import scala.Math.sqrt
import scala.compat.Platform

/**
 * Control the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class SteadyHarness(CLASSNAME: String, CLASSPATH: String, WARMUP: Int, RUNS: Int, MULTIPLIER: Int) extends Harness {

	private var benchmarkMainMethod: Method = null

	/**
	 * Function runStartupState
	 * Measure startup running time
	 */
	override def run() {

		val args = { null }
		var mulTimeStart: Long = 0
		var mulTimeEnd: Long = 0

		benchmarkMainMethod = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME).getMethod("main", classOf[Array[String]])

		println("[Warm Up] ")

		for (mul <- 1 to MULTIPLIER) {
			Platform.collectGarbage
			
			timeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			timeEnd = Platform.currentTime

			TimeSeries ::= timeEnd - timeStart

		}
		statistic = new Statistic(TimeSeries)
		println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);

		while (statistic.CoV >= steadyThreshold) {
			Platform.collectGarbage
			
			timeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			timeEnd = Platform.currentTime

			TimeSeries = TimeSeries.tail ++ List(timeEnd - timeStart)
			statistic = new Statistic(TimeSeries)
			println("[Newest] " + TimeSeries.last)
			println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);
		}

		println("[Steady State] ")

		TimeSeries = Nil

		for (mul <- 1 to MULTIPLIER) {
			Platform.collectGarbage
			
			timeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			timeEnd = Platform.currentTime

			TimeSeries ::= timeEnd - timeStart
		}

		statistic = new Statistic(TimeSeries)
		constructStatistic
	}

	override def constructStatistic() {

		val Mean = statistic.Mean()
		val ConfidencInterval = statistic.ConfidentInterval()
		val diff = (ConfidencInterval.last - ConfidencInterval.head) / 2
		
		for (i <- TimeSeries) {
			println("[Running Time] 	" + i + "ms")
		}
		println("[Average]	" + Mean.formatted("%.2f") + "ms")
		println("[Confident Intervals]	[" + ConfidencInterval.head.formatted("%.2f") + "; " + ConfidencInterval.last.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / Mean * 100).formatted("%.2f") + "%")
	}

}