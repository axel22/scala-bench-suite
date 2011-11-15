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
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression

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
            declaringClass: String,
            diggingMethod: String,
            instrumentedOut: Directory,
            backup: Directory): BottleneckFinder =
    new BottleneckDiggingFinder(
      config,
      log,
      benchmark,
      declaringClass,
      diggingMethod,
      instrumentedOut,
      backup)

  def apply(config: Config,
            log: Log,
            benchmark: PinpointBenchmark,
            declaringClass: String,
            bottleneckMethod: String,
            callIndexList: List[Int],
            callList: List[MethodCallExpression],
            instrumentedOut: Directory,
            backup: Directory): BottleneckFinder =
    new BottleneckBinaryFinder(
      config,
      log,
      benchmark,
      declaringClass,
      bottleneckMethod,
      callIndexList,
      callList,
      instrumentedOut,
      backup)

}
