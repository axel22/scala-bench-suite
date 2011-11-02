/*
 * PerformanceBenchmarkTemplate
 * 
 * Version
 * 
 * Created on October 30th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.benchmark.BenchmarkTemplate

trait PerformanceBenchmarkTemplate extends BenchmarkTemplate {

  val runs = 1

  val multiplier = 2

}
