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
	private var diff: Double = 0

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

		Platform.collectGarbage

		for (mul <- 1 to MULTIPLIER) {
			timeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			timeEnd = Platform.currentTime

			TimeSeries ::= timeEnd - timeStart

		}
		println("[Standard Deviation] " + StandardDeviation(MULTIPLIER) + "	[Sample Mean] " + Mean.formatted("%.2f") + "	[CoV] " + CoV(MULTIPLIER));

		while (CoV(MULTIPLIER) >= steadyThreshold) {
			timeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			timeEnd = Platform.currentTime

			TimeSeries = TimeSeries.tail ++ List(timeEnd - timeStart)
			println("[Newest] " + TimeSeries.last)
			println("[Standard Deviation] " + StandardDeviation(MULTIPLIER) + "	[Sample Mean] " + Mean.formatted("%.2f") + "	[CoV] " + CoV(MULTIPLIER));
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

		constructStatistic
	}

	override def constructStatistic() {

		Mean = ConstructMean(MULTIPLIER)

		if (RUNS >= 30) {
			diff = getGaussian(alpha) * StandardDeviation(MULTIPLIER) / sqrt(MULTIPLIER)
		} else {
			diff = getStudent(alpha) * StandardDeviation(MULTIPLIER) / sqrt(MULTIPLIER)
		}

		CILeft = Mean - diff
		CIRight = CILeft + 2 * diff
	}

	def printOuput() {

		var sum: Long = 0
		var x: Long = 0
		for (i <- TimeSeries) {
			println("[Running Time] 	" + i + "ms")
		}

		println("[Sample Mean]	" + Mean.formatted("%.2f") + "ms")
		println("[Confident Intervals]	[" + CILeft.formatted("%.2f") + "; " + CIRight.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / Mean * 100).formatted("%.2f") + "%")
	}
}