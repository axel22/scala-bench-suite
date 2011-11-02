/*
 * MethodPerformanceChecker
 * 
 * Version
 * 
 * Created on October 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.regression.RegressionResult

/** Detects performance regression of a specific method.
 */
trait MethodPerformanceChecker {

  def check(benchmark: Benchmark): RegressionResult

}
