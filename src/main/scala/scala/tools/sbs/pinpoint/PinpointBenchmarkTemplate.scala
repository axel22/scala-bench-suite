/*
 * PinpointBenchmarkTemplate
 * 
 * Version
 * 
 * Created on October 31th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.nsc.io.Directory
import scala.tools.sbs.performance.PerformanceBenchmarkTemplate

trait PinpointBenchmarkTemplate extends PerformanceBenchmarkTemplate {

  val pinpointClass = ""

  val pinpointMethod = ""

  val pinpointExclude: List[String] = Nil

  val pinpointPrevious = Directory(".pinpointprevious")

  val pinpointDepth = -1

}
