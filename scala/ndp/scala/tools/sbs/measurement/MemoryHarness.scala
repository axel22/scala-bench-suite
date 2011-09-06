/*
 * MemoryHarness
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package measurement

import java.lang.reflect.Method

import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log

/**
 * Class represents the harness controls the runtime for measuring memory consumption.
 *
 * @author ND P
 */
object MemoryHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark class and its <code>main</code> method from .class file using reflection.
   * <li>Iterates the loading of benchmark class and the invoking of <code>main</code> for memory consumption to be stable.
   * <li>Measures and stores the result to file.
   * </ul>
   */
  def main(args: Array[String]): Unit = {

    try {
      val confArgs = args(0) split " "
      val logArgs = args(1) split " "

      for (c <- confArgs) {
        println(c)
      }
      for (l <- logArgs) {
        println(l)
      }
      
      config = new Config(confArgs)

      log = new Log(logArgs)

      log("[Benchmarking memory consumption]")

      val runtime: Runtime = Runtime.getRuntime
      var clazz: Class[_] = null
      var method: Method = null

      val result = runBenchmark(
        (result: BenchmarkResult) => {
          var i: Int = 1
          while ((i < result.length) && (result(i) == result.head)) {
            i += 1
          }
          if (i == result.length) true else false
        },
        {
          val start = runtime.freeMemory
          clazz = Class forName config.classname
          method = clazz.getMethod("main", classOf[Array[String]])
          method.invoke(clazz, { null })
          val end = runtime.freeMemory

          clazz = null
          method = null

          start - end
        }
      )

      for (ret <- result) {
        Console println ret
      }

      System exit 0
    } catch {
      case e => {
        println(e.toString)
        println(e.getStackTraceString)
        System exit 1
      }
    }
  }

}
