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

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.Log

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

  def sampleNumber: Int

  def createLog(mode: BenchmarkMode): Log

  /** Maximum time for each benchmarking, default to 15 seconds.
   */
  def timeout: Int

  /** Sets the running context and load benchmark classes.
   */
  def init()

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run()

  /** Resets the context.
   */
  def reset()

  /** Class loader
   */
  def context: ClassLoader

  /** Produces a XML element representing this benchmark.
   */
  def toXML: scala.xml.Elem

}
