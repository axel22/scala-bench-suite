/*
 * MemoryHarness
 * 
 * Version 
 * 
 * Created on August 17th 2011
 *
 * Created by ND P
 */

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

import scala.Math.sqrt
import scala.compat.Platform

/**
 * Class represent the harness controls the runtime for measuring memory consumption.
 *
 * @author ND P
 */
class MemoryHarness(CLASSNAME: String, CLASSPATH: String, RUNS: Int, MULTIPLIER: Int) extends Harness {

	/**
	 * The <code>Runtime</code> class used to get information from the environment in which the benchmark is running.
	 */
	private val runtime: Runtime = Runtime.getRuntime
	/**
	 * The <code>List</code> of scala class contains many of benchmark class.
	 */
	private var clazz: List[Class[_]] = Nil
	/**
	 * The <code>List</code> of scala method contains many of benchmark <code>main</code> method.
	 */
	private var methods: List[Method] = Nil
	/**
	 * The thredshold used to determine whether the given <code>main</code> method has reached the steady state.
	 */
	private val steadyThreshold: Double = 0.01

	/**
	 * Does the following:
	 * <ul>
	 * <li>Loads the benchmark <code>main</code> method from .class file using reflection.
	 * <li>Iterates the loading of benchmark class.
	 * <li>Iterates the loading of benchmark <code>main</code> method.
	 * <li>Iterates the invoking of benchmark <code>main</code> method to measures its memory consumption.
	 * <li>And stores the result to file.
	 * </ul>
	 */
	override def run() {

		println("[Warm up]")
		Platform.collectGarbage

		for (mul <- 1 to MULTIPLIER) {

			start = runtime.totalMemory - runtime.freeMemory

			for (i <- 1 to RUNS) {
				clazz ::= (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
			}
			for (i <- 1 to RUNS) {
				methods ::= clazz.head.getMethod("main", classOf[Array[String]])
			}
			for (i <- 1 to RUNS) {
				methods.head.invoke(null, { null })
			}

			end = runtime.totalMemory - runtime.freeMemory

			Series ::= end - start

			clazz = Nil
			methods = Nil
			Platform.collectGarbage
		}

		statistic = new Statistic(Series)
		println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);

		while (statistic.CoV >= steadyThreshold) {
			
			clazz = Nil
			methods = Nil
			Platform.collectGarbage
			
			start = runtime.totalMemory - runtime.freeMemory

			for (i <- 1 to RUNS) {
				clazz ::= (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
			}
			for (i <- 1 to RUNS) {
				methods ::= clazz.head.getMethod("main", classOf[Array[String]])
			}
			for (i <- 1 to RUNS) {
				methods.head.invoke(null, { null })
			}

			end = runtime.totalMemory - runtime.freeMemory

			Series = Series.tail ++ List(end - start)
			
			statistic.setSERIES(Series)
			println("[Newest] " + Series.last)
			println("[Standard Deviation] " + statistic.StandardDeviation + "	[Sample Mean] " + statistic.Mean.formatted("%.2f") + "	[CoV] " + statistic.CoV);
		}

		println("[Steady State] ")

		Series = Nil

		Platform.collectGarbage

		for (mul <- 1 to MULTIPLIER) {

			start = runtime.totalMemory - runtime.freeMemory

			for (i <- 1 to RUNS) {
				clazz ::= (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
			}
			for (i <- 1 to RUNS) {
				methods ::= clazz.head.getMethod("main", classOf[Array[String]])
			}
			for (i <- 1 to RUNS) {
				methods.head.invoke(null, { null })
			}

			end = runtime.totalMemory - runtime.freeMemory

			Series ::= end - start

			clazz = Nil
			methods = Nil
			Platform.collectGarbage
		}

		statistic.setSERIES(Series)
		constructStatistic

		result = new BenchmarkResult(Series, CLASSNAME, false)
		result.storeByDefault
	}

}
