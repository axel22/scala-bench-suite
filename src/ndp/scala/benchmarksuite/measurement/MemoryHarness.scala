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

import ndp.scala.benchmarksuite.regression.Persistor
import ndp.scala.benchmarksuite.regression.BenchmarkResult
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
    var result: BenchmarkResult = new BenchmarkResult

    val warmmax = 10
    var warmup = false

    log("[Benchmarking memory consumption]")

    log verbose "[Warmup]"

    log debug config.toString

    try {
      for (i <- 1 to warmmax) {

        cleanUp

        start = runtime.freeMemory
        clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH + config.FILE_SEPARATOR)))).loadClass(config.CLASSNAME)
        method = clazz.getMethod("main", classOf[Array[String]])
        method.invoke(clazz, { null })
        end = runtime.freeMemory

        result += start - end

        log verbose "[Measured]	" + result.last

        clazz = null
        method = null
      }

      while (!warmup) {

        cleanUp

        start = runtime.freeMemory
        clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH + config.FILE_SEPARATOR)))).loadClass(config.CLASSNAME)
        method = clazz.getMethod("main", classOf[Array[String]])
        method.invoke(clazz, { null })
        end = runtime.freeMemory

        log verbose "[Measured]	" + result.last

        result remove 0
        result += start - end

        log debug "[Result]	" + result.toString

        clazz = null
        method = null

        warmup = true
        for (i <- result) {
          if (i != result.last) {
            warmup = false
          }
        }
      }

      log verbose "[Steady State]"

      cleanUp

      start = runtime.freeMemory
      clazz = (new URLClassLoader(Array(new URL("file:" + config.CLASSPATH + config.FILE_SEPARATOR)))).loadClass(config.CLASSNAME)
      method = clazz.getMethod("main", classOf[Array[String]])
      method.invoke(clazz, { null })
      end = runtime.freeMemory

      log verbose "[Measured]	" + (start - end)

      result += start - end

      constructStatistic(log, config, result)
      
      log verbose "[End constructing statistical metric]"

      detectRegression(log, config, result)
      
      (new Persistor(log, config) += result).store

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
