/*
 * InstrumentingResult
 * 
 * Version
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import scala.tools.sbs.benchmark.Benchmark
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.util.Constant

/** A {@link RunResult} from a running of {@link Runner}.
 *  In the mean time, also a {@link BenchmarkResult} for reporting.
 */
trait InstrumentingResult extends BenchmarkResult {

  def mode = Instrumenting

}

case class InstrumentingSuccess(benchmark: InstrumentingBenchmark, instrumentResult: InstrumentResult)
  extends BenchmarkSuccess with InstrumentingResult {

  def benchmarkName = benchmark.name

  def toReport = ArrayBuffer(Constant.INDENT + "TODO")
}

trait InstrumentingFailure extends BenchmarkFailure with InstrumentingResult

class InstrumentingException(_benchmark: InstrumentingBenchmark, exception: Exception)
  extends ExceptionBenchmarkFailure(_benchmark.name, exception)
  with InstrumentingFailure {

  def benchmark = _benchmark

}

object InstrumentingException {

  def apply(benchmark: InstrumentingBenchmark, exception: Exception) = new InstrumentingException(benchmark, exception)

  def unapply(pe: InstrumentingException): Option[(InstrumentingBenchmark, Exception)] =
    Some((pe.benchmark, pe.exception))

}
