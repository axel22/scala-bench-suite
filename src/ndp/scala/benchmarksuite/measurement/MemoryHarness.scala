/*
 * MemoryHarness
 * 
 * Version 
 * 
 * Created on August 17th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite
package measurement

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

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

    log("[Benchmarking memory consumption]")

    val runtime: Runtime = Runtime.getRuntime
    var clazz: Class[_] = null
    var method: Method = null

    runBenchmark(
      log,
      config,
      (result: BenchmarkResult) => {
        var i: Int = 1
        while ((i < result.length) && (result(i) == result.head)) {
          i += 1
        }
        if (i == result.length) true else false
      },
      {
        val start = runtime.freeMemory
        clazz = (new URLClassLoader(Array(new URL("file:" + config.BENCHMARK_DIR + config.FILE_SEPARATOR)))).loadClass(config.CLASSNAME)
        method = clazz.getMethod("main", classOf[Array[String]])
        method.invoke(clazz, { null })
        val end = runtime.freeMemory

        clazz = null
        method = null

        start - end
      }
    )
  }

}
