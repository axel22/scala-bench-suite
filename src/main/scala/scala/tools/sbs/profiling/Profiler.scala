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

  def run(benchmark: Benchmark): RunResult = profile(benchmark)

  def profile(benchmark: Benchmark): ProfilingResult

}

object ProfilerFactory {

  def apply(config: Config): Runner = {
    new JDIProfiler(config)
  }

}
