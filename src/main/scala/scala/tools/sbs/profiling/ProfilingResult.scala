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

import scala.tools.sbs.benchmark.Benchmark
import scala.collection.mutable.ArrayBuffer

/** A {@link RunResult} from a running of {@link Runner}.
 *  In the mean time, also a {@link BenchmarkResult} for reporting.
 */
trait ProfilingResult extends BenchmarkResult {

  def mode = Profiling

}

case class ProfilingSuccess(benchmark: ProfilingBenchmark, profile: Profile)
  extends BenchmarkSuccess with ProfilingResult {

  def benchmarkName = benchmark.name

  def toReport =
    (profile.classes flatMap (_.toReport)) ++
      ArrayBuffer(
        "Steps performed: " + profile.steps,
        "Boxing: " + profile.boxing,
        "Unboxing: " + profile.unboxing) ++
        profile.memoryActivity.toReport

}

trait ProfilingFailure extends BenchmarkFailure with ProfilingResult

class ProfilingException(_benchmark: ProfilingBenchmark, exception: Exception)
  extends ExceptionBenchmarkFailure(_benchmark.name, exception)
  with ProfilingFailure {

  def benchmark = _benchmark

}

object ProfilingException {

  def apply(benchmark: ProfilingBenchmark, exception: Exception) = new ProfilingException(benchmark, exception)

  def unapply(pe: ProfilingException): Option[(ProfilingBenchmark, Exception)] =
    Some((pe.benchmark, pe.exception))

}
