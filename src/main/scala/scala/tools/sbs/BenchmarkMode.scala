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

/** Benchmarking modes, includes:
 *  <ul>
 *  <li>Benchmarking in startup state
 *  <li>Benchmarking in steady state
 *  <li>Measuring memory usage
 *  <li>Profiling
 *  <li>Pinpointing
 *  </ul>
 */
trait BenchmarkMode {

  /** Path from benchmark directory to save logs,
   *  and from history directory to save measurement results.
   */
  def location: String

}

object StartUpState extends BenchmarkMode {

  val location = "startup"

  override val toString = "startup"

}

object SteadyState extends BenchmarkMode {

  val location = "steady"

  override val toString = "steady"

}

object MemoryUsage extends BenchmarkMode {

  val location = "memory"

  override val toString = "memory"

}

object Profiling extends BenchmarkMode {

  val location = "profile"

  override val toString = "profile"

}

object Pinpointing extends BenchmarkMode {

  val location = "pinpoint"

  override val toString = "pinpoint"

}
