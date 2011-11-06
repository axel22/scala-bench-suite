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

  def description: String

}

object StartUpState extends BenchmarkMode {

  val location = "startup"

  override val toString = "startup"

  val description = "Benchmarking performance in start-up state"

}

object SteadyState extends BenchmarkMode {

  val location = "steady"

  override val toString = "steady"

  val description = "Benchmarking performance in steady state"

}

object MemoryUsage extends BenchmarkMode {

  val location = "memory"

  override val toString = "memory"

  val description = "Benchmarking memory consumption in steady state"

}

object Profiling extends BenchmarkMode {

  val location = "profile"

  override val toString = "profile"

  val description = "Profiling"

}

object Instrumenting extends BenchmarkMode {

  val location = "instrument"

  override val toString = "instrument"

  val description = "Instrumenting"

}

object Pinpointing extends BenchmarkMode {

  val location = "pinpoint"

  override val toString = "pinpoint"

  val description = "Pinpointing regression detection"

}
