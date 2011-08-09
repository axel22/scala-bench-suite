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
import scala.compat.Platform

/**
 * Control the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class StartupHarness(CLASSNAME: String, CLASSPATH: String, WARMUP: Int, RUNS: Int, MULTIPLIER: Int) extends Harness {

	private var processBuilder: ProcessBuilder = null
	private var process: Process = null
	private var diff: Double = 0

	/**
	 * Function runStartupState
	 * Measure startup running time
	 */
	override def run() {

		processBuilder = new ProcessBuilder("scala.bat", "-classpath", CLASSPATH, CLASSNAME)

		// Ignore the first launch due to system status changing
		process = processBuilder.start
		process.waitFor

		for (i <- 1 to RUNS) {
			timeStart = Platform.currentTime
			process = processBuilder.start
			process.waitFor
			timeEnd = Platform.currentTime
			runningTimes ::= timeEnd - timeStart
		}

		CalculateStatistic
	}

	override def CalculateStatistic() {

		sampleMean = SampleMean(RUNS)

		if (RUNS >= 30) {
			diff = getGaussian(alpha) * StandardDeviation(RUNS) / sqrt(RUNS)
		} else {
			diff = getStudent(alpha) * StandardDeviation(RUNS) / sqrt(RUNS)
		}

		CILeft = sampleMean - diff
		CIRight = CILeft + 2 * diff
	}

	def SampleMean = sampleMean

	def printOuput() {
		
		for (i <- runningTimes) {
			println("[Running Time] 	" + i + "ms")
		}

		println("[Sample Mean]	" + sampleMean.formatted("%.2f") + "ms")
		println("[Confident Intervals]	[" + CILeft.formatted("%.2f") + "; " + CIRight.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / sampleMean * 100).formatted("%.2f") + "%")
	}
}