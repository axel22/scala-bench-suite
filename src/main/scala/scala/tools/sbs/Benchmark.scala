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

import java.net.URL

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
import scala.tools.sbs.io.Log

trait Benchmark {

  lazy val name: String = src.stripExtension

  /** Path to the benchmark source file / directory.
   */
  def src: Path

  /** Arguments of the benchmark.
   */
  def arguments: List[String]

  /** Classpath of the benchmark.
   */
  def classpathURLs: List[URL]

  def runs: Int

  def multiplier: Int

  def sampleNumber: Int

  def shouldCompile: Boolean

  def log: Log

  /** Sets the running context and load benchmark classes.
   */
  def init()

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run()

  /** Resets the context.
   */
  def reset()

  /** Creates the process command for start up benchmarking.
   */
  def initCommand(): Boolean

  /** Runs the benchmark process.
   */
  def runCommand()

  /** Produces a XML element representing this benchmark.
   */
  def toXML: scala.xml.Elem

}

object BenchmarkFactory {

  def apply(src: Path,
            arguments: List[String],
            classpathURLs: List[URL],
            runs: Int,
            multiplier: Int,
            sampleNumber: Int,
            shouldCompile: Boolean,
            config: Config): Benchmark =
    new SnippetBenchmark(src, arguments, classpathURLs, runs, multiplier, sampleNumber, shouldCompile, config)

}
