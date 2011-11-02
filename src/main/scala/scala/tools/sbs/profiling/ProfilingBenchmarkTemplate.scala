/*
 * ProfilingBenchmarkTemplate
 * 
 * Version
 * 
 * Created on October 31th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.tools.sbs.benchmark.BenchmarkTemplate

trait ProfilingBenchmarkTemplate extends BenchmarkTemplate {

  val profileClasses: List[String] = Nil

  val profileExclude: List[String] = Nil

  val profileMethod = ""

  val profileField = ""

}
