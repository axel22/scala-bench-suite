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

import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.io.Log

/** Trait for some kinds of profiling.
 */
trait Profiler {

  def profile(benchmark: Benchmark): Profile

}

object ProfilerFactory {

  def apply(log: Log, config: Config): Profiler = new JDIProfiler(log, config)

}
