/*
 * MemoryHarness
 * 
 * Version 
 * 
 * Created on August 17th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

import scala.collection.mutable.ArrayBuffer

import ndp.scala.benchmarksuite.utility.BenchmarkResult
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log

/**
 * Class represents the harness controls the runtime for measuring memory consumption.
 *
 * @author ND P
 */
class MemoryHarness(log: Log, config: Config) extends Harness(log, config) {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark class and its <code>main</code> method from .class file using reflection.
   * <li>Iterates the loading of benchmark class and the invoking of <code>main</code> for memory consumption to be stable.
   * <li>Measures and stores the result to file.
   * </ul>
   */
  override def run(): BenchmarkResult = {

    val runtime: Runtime = Runtime.getRuntime
    val steadyThreshold = 0.01
    var clazz: Class[_] = null
    var method: Method = null

    var start: Long = 0
    var end: Long = 0
    var series: ArrayBuffer[Long] = new ArrayBuffer
    var result: BenchmarkResult = null

    val warmmax = 30
    var warmup = false

    log("[Benchmarking memory consumption]")

    log debug "[Warmup]"

    try {
      for (i <- 1 to warmmax) {

        cleanUp

        start = runtime.freeMemory
        clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH)))).loadClass(config.CLASSNAME)
        method = clazz.getMethod("main", classOf[Array[String]])
        method.invoke(clazz, { null })
        end = runtime.freeMemory

        series += start - end

        clazz = null
        method = null
      }

      while (!warmup) {

        cleanUp

        start = runtime.freeMemory
        clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH)))).loadClass(config.CLASSNAME)
        method = clazz.getMethod("main", classOf[Array[String]])
        method.invoke(clazz, { null })
        end = runtime.freeMemory

        println(start - end)

        series.remove(0)
        series += start - end

        clazz = null
        method = null

        warmup = true
        for (i <- series) {
          if (i != series.last) {
            warmup = false
          }
        }
      }

      log.debug("[Steady State]")

      cleanUp

      start = runtime.freeMemory
      clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH)))).loadClass(config.CLASSNAME)
      method = clazz.getMethod("main", classOf[Array[String]])
      method.invoke(clazz, { null })
      end = runtime.freeMemory

      println(start - end)

      series += start - end

      constructStatistic(log, series)

      result = new BenchmarkResult(series, config.CLASSNAME, false)
      result.storeByDefault
      result
    } catch {
      case e: java.lang.reflect.InvocationTargetException => {
        e.getCause match {
          case n: java.lang.ClassNotFoundException => {
            log("Class " + n.getMessage() + " not found. Please check the class directory.")
            null
          }
          case n: java.lang.NoClassDefFoundError => {
            log("Class " + n.getMessage() + " not found. Please check the class directory.")
            null
          }
          case n => throw n
        }
      }
      case i => throw i
    }
  }

}
