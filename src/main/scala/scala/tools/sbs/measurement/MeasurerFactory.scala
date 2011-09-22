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

import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.BenchmarkMode.MEMORY
import scala.tools.sbs.benchmark.BenchmarkMode.STARTUP
import scala.tools.sbs.benchmark.BenchmarkMode.STEADY
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class MeasurerFactory(log: Log, config: Config) {

  def create(mode: BenchmarkMode): Measurer = mode match {
    case STEADY => new SteadyHarness
    case MEMORY => new MemoryHarness
    case STARTUP => new StartupHarness(log, config)
  }

}
