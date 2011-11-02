/*
 * MethodScrutinizer
 * 
 * Version
 * 
 * Created on October 13th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.nsc.io.Path.string2path
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI

class MethodScrutinizer(protected val config: Config, protected val log: Log) extends Scrutinizer {

  val instrumentedURL = config.bin / ".instrumented" createDirectory () toURL

  def scrutinize(benchmark: PinpointBenchmark): ScrutinyResult = {
    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)

    val detector = ScrutinyRegressionDetectorFactory(config, log, benchmark, instrumentor, instrumentedURL)

    detector detect benchmark match {
      case regressionSuccess: ScrutinyCIRegressionSuccess => regressionSuccess
      case regressionFailure: ScrutinyRegressionFailure => if (config.pinpointBottleneckDectect) {
        val bottleneckFound = BottleneckFinderFactory(config, log, benchmark, instrumentor, instrumentedURL).find()
        
        bottleneckFound.toReport foreach UI.info
        
        bottleneckFound.toReport foreach log.info
        bottleneckFound
      }
      else {
        regressionFailure
      }
    }
  }

}
