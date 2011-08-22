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
import java.lang.Thread.sleep

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
	 * The benchmark class loaded.
	 */
	private var clazz: Class[_] = null
	/**
	 * The <code>main</code> method of the benchmark class.
	 */
	private var method: Method = null
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

		println("[Warmup]	")
		cleanUp
		start = runtime.freeMemory
		clazz = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
		method = clazz.getMethod("main", classOf[Array[String]])
		method.invoke(null, { null })
		end = runtime.freeMemory

		println(start - end)

		cleanUp
		start = runtime.freeMemory
		clazz = (new URLClassLoader(Array(new URL("file:" + CLASSPATH)))).loadClass(CLASSNAME)
		method = clazz.getMethod("main", classOf[Array[String]])
		method.invoke(null, { null })
		end = runtime.freeMemory

		println(start - end)

		Series ::= start - end

		result = new BenchmarkResult(Series, CLASSNAME, false)
		result.storeByDefault
	}

	/**
	 * Forces the Java gc to clean up the heap.
	 */
	def cleanUp() {
		Platform.collectGarbage
		System.runFinalization
		sleep(100)
		Platform.collectGarbage
		System.runFinalization
		sleep(100)
		Platform.collectGarbage
	}
}
