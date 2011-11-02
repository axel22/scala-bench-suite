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

import java.net.URL

import scala.tools.sbs.io.Log

trait ScrutinyRegressionDetector {

  def detect(benchmark: PinpointBenchmark): ScrutinyRegressionResult

}

object ScrutinyRegressionDetectorFactory {

  def apply(config: Config,
            log: Log,
            benchmark: PinpointBenchmark,
            instrumentor: CodeInstrumentor, instrumentedURL: URL): ScrutinyRegressionDetector =
    new MethodRegressionDetector(config, log, benchmark, instrumentor, instrumentedURL)

}
