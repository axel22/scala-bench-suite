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

/**
 * Class represents the harness controls the runtime for measuring memory consumption.
 *
 * @author ND P
 */
object MemoryHarness extends SubProcessHarness {

  /**
   * Does the following:
   * <ul>
   * <li>Loads the benchmark class and its <code>main</code> method from .class file using reflection.
   * <li>Iterates the loading of benchmark class and the invoking of <code>main</code> for memory consumption to be stable.
   * <li>Measures and stores the result to file.
   * </ul>
   */
  def run(): Either[BenchmarkResult, String] = {

    log("[Benchmarking memory consumption]")

    val runtime: Runtime = Runtime.getRuntime
    runBenchmark(
      series => (series map (t => t == series.head) filter (b => b)).length == series.length,
      {
        val start = runtime.freeMemory
        benchmark.init()
        benchmark.run()
        benchmark.finallize()
        start - runtime.freeMemory
      }
    )
  }
}
