/*
 * BottleneckFinder
 * 
 * Version
 * 
 * Created on October 27th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import java.net.URL

import scala.tools.sbs.io.Log

/** Uses instrumentation method to point out the method call
 *  that is a performance bottleneck in a given method.
 */
trait BottleneckFinder {

  def find(): BottleneckFound

}

object BottleneckFinderFactory {

  def apply(config: Config,
            log: Log,
            benchmark: PinpointBenchmark,
            instrumentor: CodeInstrumentor,
            instrumentedURL: URL): BottleneckFinder =
    new BottleneckBinaryFinder(log, config, benchmark, instrumentor, instrumentedURL)

}
