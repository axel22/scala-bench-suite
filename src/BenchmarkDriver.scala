/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on May 25th 2011
 *
 * By ND P
 */

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

import scala.compat.Platform
import scala.Math.sqrt

/**
 * Control the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {
	/**
	 * Start point of the benchmark driver
	 */
	def main(args: Array[String]): Unit = {

		try {
			var benchmarkRunner = new BenchmarkRunner(args(0), args(1) + "\\", args(2).toInt, args(3).toInt, args(4).toInt)

			benchmarkRunner.run

			/*for (i <- benchmarkRunner.allStartup) {
				println("[Running Time] 	" + i + "ms")
			}
			println("[Sample Mean]	" + benchmarkRunner.sampleMeanStartup + "ms")*/

		} catch {
			case e: Exception => throw e
		}
	}
}

class BenchmarkRunner(CLASSNAME: String, CLASSPATH: String, WARMUP: Int, RUNS: Int, MULTIPLIER: Int) {

	private var processBuilder: ProcessBuilder = null
	private var process: Process = null
	private var benchmarkMainMethod: Method = null
	private var timeStartStartup: Long = 0
	private var timeStartSteady: Long = 0
	private var timeEndStartup: Long = 0
	private var timeEndSteady: Long = 0
	private var runningTimesStartup: List[Long] = List()
	private var runningTimesSteady: List[Long] = List()

	var sampleMeanStartup: Double = 0
	var CIStartupLeft: Double = 0
	var CIStartupRight: Double = 0

	val alpha = 0.05
	val z = 1.96

	/**
	 * Do the warm up and measure running time of the class snippet
	 */
	def run(): Unit = {

		runStartupState
		//runSteadyState
		println
	}

	def runStartupState {

		var s: Double = 0
		var diff: Double = 0

		processBuilder = new ProcessBuilder("D:\\University\\5thYear\\Internship\\scala-2.9.0.1\\bin\\scala.bat", "-classpath", CLASSPATH, CLASSNAME)

		// Ignore the first launch due to system status changing
		process = processBuilder.start
		process.waitFor

		for (i <- 1 to RUNS) {
			timeStartStartup = Platform.currentTime
			process = processBuilder.start
			process.waitFor
			timeEndStartup = Platform.currentTime
			runningTimesStartup ::= timeEndStartup - timeStartStartup
			// printOutput
		}

		sampleMeanStartup = average(runningTimesStartup, RUNS)
		s = getStandardDeviation(runningTimesStartup, RUNS)

		if (RUNS >= 30) {
			diff = getGaussian(alpha) * s / sqrt(RUNS)
		} else {
			diff = getStudent(alpha) * s / sqrt(RUNS)
		}

		CIStartupLeft = sampleMeanStartup - diff
		CIStartupRight = sampleMeanStartup + diff

		for (i <- runningTimesStartup) {
			println("[Running Time] 	" + i + "ms")
		}

		println("[Sample Mean]	" + sampleMeanStartup.formatted("%.2f") + "ms")
		println("[Confident Intervals]	[" + CIStartupLeft.formatted("%.2f") + "; " + CIStartupRight.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / sampleMeanStartup * 100).formatted("%.2f") + "%")
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
	 * Function runSteadyState
	 */
	def runSteadyState {
		val args = { null }
		var mulTimeStart: Long = 0
		var mulTimeEnd: Long = 0

		this.benchmarkMainMethod = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME).getMethod("main", classOf[Array[String]])

		println("[Warm Up] ")

		Platform.collectGarbage

		timeStartSteady = Platform.currentTime
		for (i <- 1 to WARMUP) {
			benchmarkMainMethod.invoke(null, args)
		}
		timeEndSteady = Platform.currentTime

		println("[Steady State] ")

		Platform.collectGarbage

		timeStartSteady = Platform.currentTime
		timeEndSteady = timeStartSteady
		for (mul <- 1 to MULTIPLIER) {
			mulTimeStart = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			mulTimeEnd = Platform.currentTime

			runningTimesSteady ::= mulTimeEnd - mulTimeStart

			timeEndSteady += mulTimeEnd - mulTimeStart
		}
	}

	/**
	 * @return the minimum running time
	 */
	def getMinTime(runningTimes: List[Long]): Long = {
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
	def getMaxTime(runningTimes: List[Long]): Long = {
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
	def average(runningTimes: List[Long], runs: Int): Double = {
		getRunTime(runningTimes).asInstanceOf[Double] / runs
	}

	/**
	 * @return the total running time
	 */
	def getRunTime(runningTimes: List[Long]) = {
		var sum: Double = 0
		for (i <- runningTimes) {
			sum += i
		}
		sum
	}

	/**
	 * @return all the running times of startup benchmarking
	 */
	def allStartup = runningTimesStartup
	/**
	 * @return all the running times of multipliers
	 */
	def allSteady = runningTimesSteady

	/**
	 * @return the standard deviation of repetitions
	 */
	def getStandardDeviation(runningTimes: List[Long], runs: Int): Double = {
		var squareSum: Double = 0
		val sampleMean = average(runningTimes, runs)
		for (i <- runningTimes) {
			squareSum += (i - sampleMean) * (i - sampleMean)
		}
		sqrt(squareSum / (runs - 1))
	}
}