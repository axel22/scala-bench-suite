/*
 * SteadyHarness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

import scala.Math.sqrt
import scala.compat.Platform

/**
 * Class represent the harness controls the runtime of steady state benchmarking.
 *
 * @author ND P
 */
class SteadyHarness(CLASSNAME: String, CLASSPATH: String, RUNS: Int, MULTIPLIER: Int) extends Harness {

	/**
	 * The <code>Method</code> provides information about, and access to, the <code>main</code> method of the benchmark class.
	 */
	private var benchmarkMainMethod: Method = null
	/**
	 * The thredshold used to determine whether the given <code>main</code> method has reached the steady state.
	 */
	private val steadyThreshold: Double = 0.01

	/**
	 * Does the following:
	 * <ul>
	 * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
	 * <li>Iterates the invoking of benchmark <code>main</code> method for it to reach the steady state.
	 * <li>Iterates the invoking of benchmark <code>main</code> method in its steady state to measures the performance.
	 * <li>And stores the result running time series to file.
	 * </ul>
	 */
	override def run() {

		val args = { null }

		benchmarkMainMethod = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME).getMethod("main", classOf[Array[String]])

		println("[Warm Up] ")

		for (mul <- 1 to MULTIPLIER) {

			cleanUp

			start = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			end = Platform.currentTime

			Series ::= end - start

		}
		statistic = new Statistic(Series)
		//		println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);

		while (statistic.CoV >= steadyThreshold) {

			cleanUp

			start = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			end = Platform.currentTime

			Series = Series.tail ++ List(end - start)
			statistic.setSERIES(Series)
			//			println("[Newest] " + Series.last)
			//			println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);
		}

		println("[Steady State] ")

		Series = Nil

		for (mul <- 1 to MULTIPLIER) {
			
			cleanUp

			start = Platform.currentTime
			for (i <- 0 to RUNS) {
				benchmarkMainMethod.invoke(null, args)
			}
			end = Platform.currentTime

			Series ::= end - start
		}

		statistic.setSERIES(Series)
		constructStatistic

		result = new BenchmarkResult(Series, CLASSNAME, true)
		result.storeByDefault
	}

}
