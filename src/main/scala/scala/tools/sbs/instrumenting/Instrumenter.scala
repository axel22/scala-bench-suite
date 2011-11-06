/*
 * Instrumenter
 * 
 * Version 
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import scala.tools.sbs.io.Log
import scala.tools.sbs.benchmark.Benchmark

trait Instrumenter extends Runner {
  protected val upperBound = manifest[InstrumentingBenchmark]

  val benchmarkFactory = new InstrumentingBenchmarkFactory(log, config)

  protected def doBenchmarking(benchmark: Benchmark): BenchmarkResult = {
    instrument(benchmark.asInstanceOf[InstrumentingBenchmark])
  }
  
  protected def instrument(benchmark: InstrumentingBenchmark): InstrumentingResult

  /** Does nothing method.
   */
  protected def doGenerating(benchmark: Benchmark) = ()

}

object InstrumenterFactory {
  def apply(config: Config, log: Log): Instrumenter = {
    new ASMInstrumenter(config, log)
  }
}