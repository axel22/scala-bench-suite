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

			for (i <- benchmarkRunner.getAllRunningTimeStartup) {
				println("[Running Time] 	" + i + "ms")
			}
			/*println("[Total Running Time]	" + benchmarkRunner.getRunTime + "ms")
			println("[Max Running Time]	" + benchmarkRunner.getMaxTime + "ms")
			println("[Min Running Time]	" + benchmarkRunner.getMinTime + "ms")
			println("[Average Running Time]	" + benchmarkRunner.getAverageTime + "ms")
			println("[Standard Deviation]	" + benchmarkRunner.getStandardDeviation + "ms")*/
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

	/**
	 * Do the warm up and measure running time of the class snippet
	 */
	def run(): Unit = {

		runStartupState
		//runSteadyState
		println
	}

	def runStartupState {

		processBuilder = new ProcessBuilder("C:\\Program Files\\scala-2.9.0.1\\bin\\scala.bat", "-classpath", CLASSPATH, CLASSNAME)

		// Ignore the first launch due to system status changing
		process = processBuilder.start
		process.waitFor

		for (i <- 0 to RUNS) {
			timeStartStartup = Platform.currentTime
			process = processBuilder.start
			process.waitFor
			timeEndStartup = Platform.currentTime
			runningTimesStartup ::= timeEndStartup - timeStartStartup
			// printOutput
		}
	}

	/**
	 * Get the output from command line and print out
	 */
	private def printOutput: Unit = {

		try {
			var outLine: String = null
			var brCleanUp: BufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream))

			outLine = brCleanUp.readLine
			while (outLine != null) {
				println("[Startup State] " + outLine)
				outLine = brCleanUp.readLine
			}
			brCleanUp.close

			brCleanUp = new BufferedReader(new InputStreamReader(process.getErrorStream))

			outLine = brCleanUp.readLine
			while (outLine != null) {
				println("[Startup State] " + outLine)
				outLine = brCleanUp.readLine
			}
			brCleanUp.close
		} catch {
			case e: Exception => e.printStackTrace
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
	 * @return the minimum running time of each repetition
	 */
	def getMinTime: Long = {
		var result = runningTimesSteady.head
		for (i <- runningTimesSteady) {
			if (result > i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return the maximum running time of each repetition
	 */
	def getMaxTime: Long = {
		var result = runningTimesSteady.head
		for (i <- runningTimesSteady) {
			if (result < i) {
				result = i
			}
		}
		result
	}

	/**
	 * @return the average running time of repetitions
	 */
	def getAverageTime = getRunTime.asInstanceOf[Double] / MULTIPLIER

	/**
	 * @return the total running time
	 */
	def getRunTime = timeEndSteady - timeStartSteady

	/**
	 * @return all the running times of startup benchmarking
	 */
	def getAllRunningTimeStartup = runningTimesStartup
	/**
	 * @return all the running times of multipliers
	 */
	def getAllRunningTimeSteady = runningTimesSteady

	/**
	 * @return the standard deviation of repetitions
	 */
	def getStandardDeviation: Double = {
		var squareSum: Double = 0
		for (i <- runningTimesSteady) {
			squareSum += (i - getAverageTime) * (i - getAverageTime)
		}
		scala.math.sqrt(squareSum / MULTIPLIER)
	}
}