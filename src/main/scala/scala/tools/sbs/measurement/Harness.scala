/*
 * Harness
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

abstract class Harness(log: Log, config: Config) extends Measurer {

  def run(benchmark: Benchmark): MeasurementResult

  def benchmarkRunner = new BenchmarkRunner(log, config)

}
