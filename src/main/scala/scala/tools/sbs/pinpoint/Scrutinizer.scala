/*
 * Scrutinizer
 * 
 *  Version
 *  
 *  Created on October 13th, 2011
 *  
 *  Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log

trait Scrutinizer extends Runner {

  protected val upperBound = manifest[PinpointBenchmark]

  val benchmarkFactory = new PinpointBenchmarkFactory(log, config)

  protected def doBenchmarking(benchmark: Benchmark): BenchmarkResult =
    scrutinize(benchmark.asInstanceOf[PinpointBenchmark])

  protected def scrutinize(benchmark: PinpointBenchmark): ScrutinyResult

  /** Do-nothing method.
   */
  protected def doGenerating(benchmark: Benchmark) = ()

}

object ScrutinizerFactory {

  def apply(config: Config, log: Log): Scrutinizer = {
    new MethodScrutinizer(config, log)
  }

}

trait ScrutinyResult extends BenchmarkResult

trait ScrutinySuccess extends BenchmarkSuccess with ScrutinyResult

trait ScrutinyFailure extends BenchmarkFailure with ScrutinyResult
