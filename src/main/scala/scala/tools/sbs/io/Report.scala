/* 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

import scala.tools.sbs.regression.BenchmarkResult

trait Report {
  
  def apply(result: BenchmarkResult)
  
}
