/*
 * MeasurerFactory
 * 
 * Version
 * 
 * Created on Semptember 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.measurement.BenchmarkType.BenchmarkType
import scala.tools.sbs.measurement.BenchmarkType.MEMORY
import scala.tools.sbs.measurement.BenchmarkType.STARTUP
import scala.tools.sbs.measurement.BenchmarkType.STEADY
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class MeasurerFactory(log: Log, config: Config) {

  def create(metric: BenchmarkType): Measurer = metric match {
    case STEADY => new SteadyHarness(log, config)
    case STARTUP => new StartupHarness(log, config)
    case MEMORY => new MemoryHarness(log, config)
  }

}