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
 *  </ul>
 */
object BenchmarkMode extends Enumeration {
  type BenchmarkMode = Value
  val STARTUP, STEADY, MEMORY, PROFILE = Value
}
