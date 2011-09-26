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

import scala.tools.sbs.io.Log

import BenchmarkMode.BenchmarkMode

trait Measurer {

  protected var log: Log = null

  def measure(benchmark: Benchmark): MeasurementResult

}

object MeasurerFactory {

  def apply(config: Config, mode: BenchmarkMode): Measurer = mode match {
    case BenchmarkMode.STARTUP => new StartupMeasurer(config)
    case _ => new SubJVMMeasurer(config, mode)
  }

}
