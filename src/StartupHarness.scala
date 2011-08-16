/*
 * StartupHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

import scala.Math.sqrt
import scala.compat.Platform


/**
 * Class represent the harness controls the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class StartupHarness(CLASSNAME: String, CLASSPATH: String, WARMUP: Int, RUNS: Int, MULTIPLIER: Int) extends Harness {

	/**
	 * The <code>ProcessBuilder</code> used to create the operating system processes for the benchmark classes.
	 */
	private var processBuilder: ProcessBuilder = null
	/**
	 * The <code>Process</code> used to control the runtime and obtain the information of the benchmark classes. 
	 */
	private var process: Process = null

	/**
	 * Does the following:
	 * <ul>
	 * <li>Creates the operating system process for the benchmark classes to run.
	 * <li>Iterates the invoking of new JVM instance loading the benchmark classes to measures the performance.
	 * <li>And stores the result running time series to file.
	 * </ul>
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

		result = new BenchmarkResult(TimeSeries, CLASSNAME)
		result.store
	}

	override def constructStatistic() {

		val Mean = statistic.Mean()
		val ConfidenceInterval = statistic.ConfidenceInterval()
		val diff = (ConfidenceInterval.last - ConfidenceInterval.head) / 2

		for (i <- TimeSeries) {
			println("[Running Time] 	" + i + "ms")
		}
		println("[Average]	" + Mean.formatted("%.2f") + "ms")
		println("[Confident Intervals]	[" + ConfidenceInterval.head.formatted("%.2f") + "; " + ConfidenceInterval.last.formatted("%.2f") + "]")
		println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / Mean * 100).formatted("%.2f") + "%")
	}

}