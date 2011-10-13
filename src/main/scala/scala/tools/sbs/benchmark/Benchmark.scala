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

import java.net.URL

import scala.tools.sbs.io.Log
import scala.tools.sbs.BenchmarkMode

/** Represent a benchmark entity, that is an object for the suite to run, measure and detect regression.
 */
trait Benchmark {

  def name: String

  /** Arguments of the benchmark.
   */
  def arguments: List[String]

  /** Classpath of the benchmark.
   */
  def classpathURLs: List[URL]

  def runs: Int

  def multiplier: Int

  def sampleNumber: Int

  def createLog(mode: BenchmarkMode): Log

  /** Sets the running context and load benchmark classes.
   */
  def init()

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run()

  /** Resets the context.
   */
  def reset()

  /** Names of the classes to be profiled the loading.
   */
  def profiledClasses: List[String]
  
  /** Names of the classes to be ignored from profiling.
   */
  def excludeClasses: List[String]

  /** Name of the method to be profiled the invocations.
   */
  def profiledMethod: String

  /** Name of the field to be profiled the accessing and modifying.
   */
  def profiledField: String

  /** Produces a XML element representing this benchmark.
   */
  def toXML: scala.xml.Elem

}
