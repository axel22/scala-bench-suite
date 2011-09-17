/*
 * OverallHarness
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.measurement.BenchmarkType.MEMORY
import scala.tools.sbs.measurement.BenchmarkType.STARTUP
import scala.tools.sbs.measurement.BenchmarkType.STEADY
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class OverallHarness(log: Log, config: Config) {

  def run(benchmark: Benchmark): List[MeasurementResult] = {
    var ret = List[MeasurementResult]()
    benchmark.metrics foreach (_ match {
      case STEADY => ret ::= (new SteadyHarness(log, config, benchmark) run)
      case MEMORY => ret ::= (new MemoryHarness(log, config, benchmark) run)
      case STARTUP => ret ::= (new StartupHarness(log, config, benchmark) run)
    })
    ret
  }

}