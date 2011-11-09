/*
 * ScrutinyRegressionDetector
 * 
 * Version
 * 
 * Created on November 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor

trait ScrutinyRegressionDetector {

  def detect(benchmark: PinpointBenchmark): ScrutinyRegressionResult

}

object ScrutinyRegressionDetectorFactory {

  def apply(config: Config,
            log: Log,
            benchmark: PinpointBenchmark,
            instrumented: Directory,
            backup: Directory): ScrutinyRegressionDetector =
    new MethodRegressionDetector(config, log, benchmark, instrumented, backup)

}
