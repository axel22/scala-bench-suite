/*
 * PinpointMeasurer
 * 
 * Version
 * 
 * Created on October 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package strategy

import java.net.URL

import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.MeasurementResult
import scala.tools.sbs.pinpoint.PinpointBenchmark

trait PinpointMeasurer {

  def measure(benchmark: PinpointBenchmark, classpathURLs: List[URL]): MeasurementResult

}

object PinpointMeasurerFactory {

  def apply(config: Config, log: Log): PinpointMeasurer = new SubJVMPinpointMeasurer(log, config)

}
