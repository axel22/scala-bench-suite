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

/** User's benchmarks should extend this `trait` for convinience.
 */
trait BenchmarkTemplate {

  val runs = 1

  val multiplier = 2

  val sampleNumber = 0

  def init

  def run // Do something wasting time here

  def reset

}
