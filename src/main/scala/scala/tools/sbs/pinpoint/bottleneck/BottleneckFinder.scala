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
package bottleneck

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor

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
            instrumented: Directory,
            backup: Directory): BottleneckFinder =
    new BottleneckBinaryFinder(log, config, benchmark, instrumentor, instrumented, backup)

}
