/*
 * MemoryHarness
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.lang.Runtime

class MemoryHarness extends Harness with SubProcessMeasurer {

  def run(benchmark: Benchmark): MeasurementResult = {
    log.info("[Benchmarking memory consumption]")
    val runtime: Runtime = Runtime.getRuntime
    benchmarkRunner run (
      benchmark,
      series => series forall (_ == series.head),
      {
        val start = runtime.freeMemory
        benchmark.init()
        benchmark.run()
        start - runtime.freeMemory
      })
  }

}
