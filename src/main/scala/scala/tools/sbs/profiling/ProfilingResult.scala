/*
 * ProfilingResult
 * 
 * Version
 * 
 * Created on October 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.tools.sbs.common.Benchmark

/** A {@link RunResult} from a running of {@link Runner}.
 *  In the mean time, also a {@link BenchmarkResult} for reporting.
 */
abstract class ProfilingResult(benchmark: Benchmark) extends BenchmarkSuccess with RunResult {

  def mode = Profiling

}

case class ProfilingSuccess(benchmark: Benchmark, profile: Profile) extends ProfilingResult(benchmark)

abstract class ProfilingFailure(benchmark: Benchmark) extends ProfilingResult(benchmark)

case class ProfilingException(benchmark: Benchmark, exception: Exception) extends ProfilingFailure(benchmark)
