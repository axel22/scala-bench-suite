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
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor

class MethodScrutinizer(protected val config: Config, protected val log: Log) extends Scrutinizer {

  val instrumented = config.bin / ".instrumented" createDirectory ()

  val backup = config.bin / ".backup" createDirectory ()

  def scrutinize(benchmark: PinpointBenchmark): ScrutinyResult = {
    val instrumentor = CodeInstrumentor(config, log, benchmark.pinpointExclude)

    val detector = ScrutinyRegressionDetectorFactory(config, log, benchmark, instrumentor, instrumented, backup)

    detector detect benchmark match {
      case regressionSuccess: ScrutinyCIRegressionSuccess => regressionSuccess
      case regressionFailure: ScrutinyRegressionFailure => if (config.pinpointBottleneckDectect) {
        val bottleneckFound = BottleneckFinderFactory(config, log, benchmark, instrumentor, instrumented, backup).find()

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
