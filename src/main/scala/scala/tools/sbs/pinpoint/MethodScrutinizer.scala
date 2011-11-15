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
import scala.tools.sbs.pinpoint.bottleneck.BottleneckFinderFactory

class MethodScrutinizer(protected val config: Config, protected val log: Log) extends Scrutinizer {

  val instrumentedOut = config.bin / ".instrumented" createDirectory ()

  val backup = config.bin / ".backup" createDirectory ()

  def scrutinize(benchmark: PinpointBenchmark): ScrutinyResult = {

    val detector = ScrutinyRegressionDetectorFactory(config, log, benchmark, instrumentedOut, backup)

    detector detect benchmark match {
      case regressionSuccess: ScrutinyCIRegressionSuccess => regressionSuccess
      case regressionFailure: ScrutinyCIRegressionFailure if (config.pinpointBottleneckDectect) =>
        try {
          BottleneckFinderFactory(
            config,
            log,
            benchmark,
            benchmark.pinpointClass,
            benchmark.pinpointMethod,
            instrumentedOut,
            backup) find ()
        }
        catch {
          case _: MismatchExpressionList => regressionFailure
        }
      case anythingelse => anythingelse
    }
  }

}
