/*
 * Runner
 * 
 * Version
 * 
 * Created on October 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.sbs.profiling.Profiler
import scala.tools.sbs.profiling.ProfilerFactory
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.benchmark.Benchmark

/** Runs the benchmark for some purpose.
 */
trait Runner {

  protected var log: Log = null

  def run(benchmark: Benchmark): RunResult

}

object RunnerFactory {

  def apply(log: Log, config: Config, mode: BenchmarkMode): Runner = mode match {
    case Profiling => ProfilerFactory(config)
    case _         => MeasurerFactory(config, mode)
  }

}

trait RunResult

trait RunSuccess extends RunResult {

  def toXML: scala.xml.Elem

}

trait RunFailure extends RunResult
