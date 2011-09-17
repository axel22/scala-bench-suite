/*
 * Benchmark
 * 
 * Version 
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import BenchmarkMode.BenchmarkMode

trait Benchmark {

  def modes(): List[BenchmarkMode]
  
  def name(): String
  
  /**
   * Uses strange named compiler Global to compile.
   */
  def compile(): Boolean

  /**
   * Sets the running context and load benchmark classes.
   */
  def init()

  /**
   * Runs the benchmark object and throws Exceptions (if any).
   */
  def run()

  /**
   * Resets the context.
   */
  def finallize()

  /**
   * Creates the process command for start up benchmarking.
   */
  def initCommand(): Boolean

  /**
   * Runs the benchmark process.
   */
  def runCommand()

  override def toString(): String

}