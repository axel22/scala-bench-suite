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

	/**
	 * Function runStartupState
	 * Measure startup running time
	 */
	override def run() {

		processBuilder = new ProcessBuilder("scala.bat", "-classpath", CLASSPATH, CLASSNAME)

		// Ignore the first launch due to system status changing
		process = processBuilder.start
		process.waitFor

		for (i <- 1 to MULTIPLIER) {
			timeStart = Platform.currentTime
			process = processBuilder.start
			process.waitFor
			timeEnd = Platform.currentTime
			TimeSeries ::= timeEnd - timeStart
		}

		statistic = new Statistic(TimeSeries)
		constructStatistic
		
		result = new BenchmarkResult(TimeSeries)
		result.store
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