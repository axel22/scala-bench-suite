/*
 * StartupHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import scala.compat.Platform

import ndp.scala.benchmarksuite.regression.Statistic
import ndp.scala.benchmarksuite.utility.BenchmarkResult

/**
 * Class represent the harness controls the runtime of startup state benchmarking.
 *
 * @author ND P
 */
class StartupHarness(CLASSNAME: String, CLASSPATH: String, RUNS: Int, MULTIPLIER: Int) extends Harness {

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
			start = Platform.currentTime
			process = processBuilder.start
			process.waitFor
			end = Platform.currentTime
			Series ::= end - start
		}

		statistic = new Statistic(Series)
		constructStatistic

		result = new BenchmarkResult(Series, CLASSNAME, true)
		result.storeByDefault
	}

}
