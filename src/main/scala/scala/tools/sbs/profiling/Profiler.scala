/*
 * Profiler
 * 
 * Version
 * 
 * Created October 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.tools.sbs.io.Log
import scala.tools.sbs.benchmark.Benchmark

/** Trait for some kinds of profiling.
 */
trait Profiler extends Runner {

  protected val upperBound = manifest[ProfilingBenchmark]

  val benchmarkFactory = new ProfilingBenchmarkFactory(log, config)

  protected def doBenchmarking(benchmark: Benchmark): BenchmarkResult = {
    profile(benchmark.asInstanceOf[ProfilingBenchmark])
  }

  protected def profile(benchmark: ProfilingBenchmark): ProfilingResult
  
  /** Does nothing method.
   */
  protected def doGenerating(benchmark: Benchmark) = ()


}

object ProfilerFactory {

  def apply(config: Config, log: Log): Profiler = {
    new JDIProfiler(config, log)
  }

}
