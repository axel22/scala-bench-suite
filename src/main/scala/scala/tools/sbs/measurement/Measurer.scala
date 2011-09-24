/*
 * Measurer
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Cretead by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.io.Log

trait Measurer {

  def measure(benchmark: Benchmark): MeasurementResult

}

object MeasurerFactory {

  def apply(log: Log, config: Config): Measurer = new SubJVMMeasurer(log, config)

}
