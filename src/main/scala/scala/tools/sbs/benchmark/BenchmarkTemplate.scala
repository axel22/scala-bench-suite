/*
 * BenchmarkTemplate
 * 
 * Version
 * 
 * Created on October 6th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import scala.tools.nsc.io.Directory

/** User's benchmarks should extend this `trait` for convinience.
 */
trait BenchmarkTemplate {

  val sampleNumber = 0

  def init

  def run // Do something wasting time here

  def reset

}
