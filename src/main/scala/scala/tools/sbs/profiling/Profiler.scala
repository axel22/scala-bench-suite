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
trait Profiler extends Runner {

  def run(benchmark: Benchmark): RunResult = profile(benchmark)

  def profile(benchmark: Benchmark): ProfilingResult

}

object ProfilerFactory {

  def apply(config: Config): Runner = {
    println("asdf")
//    try {
    val a = new JDIProfiler(config)
    println(a)
    a
//    } catch {
//      case e => println(e)
//      null
//    }
  }

}
