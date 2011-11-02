/*
 * SubJVMPinpointMeasurer
 * 
 * Version
 * 
 * Created on October 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import java.net.URL

import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.PerformanceBenchmark
import scala.tools.sbs.measurement.SubJVMMeasurer

class SubJVMPinpointMeasurer(log: Log, config: Config)
  extends SubJVMMeasurer(log: Log, config, Pinpointing, PinpointHarness)
  with PinpointMeasurer {

  /** Bridge method.
   */
  def measure(benchmark: PinpointBenchmark, classpathURLs: List[URL]): MeasurementResult =
    measure(benchmark: PerformanceBenchmark, classpathURLs)

}
