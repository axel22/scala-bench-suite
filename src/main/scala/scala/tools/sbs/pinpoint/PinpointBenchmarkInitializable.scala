/*
 * PinpointBenchmarkInitializable
 * 
 * Version
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Path
import scala.tools.sbs.performance.PerformanceBenchmarkInitializable

class PinpointBenchmarkInitializable(name: String,
                                     classpathURLs: List[URL],
                                     src: Path,
                                     benchmarkObject: PinpointBenchmarkTemplate,
                                     context: ClassLoader,
                                     config: Config)
  extends PerformanceBenchmarkInitializable(
    name,
    classpathURLs,
    src,
    benchmarkObject,
    context,
    config)
  with PinpointBenchmark {

  def pinpointClass = benchmarkObject.pinpointClass

  def pinpointMethod = benchmarkObject.pinpointMethod

  def pinpointExclude = benchmarkObject.pinpointExclude

  def pinpointPrevious = benchmarkObject.pinpointPrevious

  def pinpointDepth = benchmarkObject.pinpointDepth

}
