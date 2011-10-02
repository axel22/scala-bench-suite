/*
 * BenchmarkMode
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package common

/** Benchmarking modes, includes:
 *  <ul>
 *  <li>Benchmarking in startup state
 *  <li>Benchmarking in steady state
 *  <li>Measuring memory usage
 *  <li>Profiling
 *  </ul>
 */
trait BenchmarkMode {

  /** Path from benchmark directory to save logs,
   *  and from history directory to save measurement results.
   */
  def location: String

}

case class StartUpState extends BenchmarkMode {

  val location = "startup"

  override val toString = "startup"

}

case class SteadyState extends BenchmarkMode {

  val location = "steady"

  override val toString = "steady"

}

case class MemoryUsage extends BenchmarkMode {

  val location = "memory"

  override val toString = "memory"

}

case class Profiling extends BenchmarkMode {

  val location = "profile"

  override val toString = "profile"

}
