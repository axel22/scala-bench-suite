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
import scala.xml.Elem

trait Benchmark {

  def name: String

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

  def apply(name: String,
            src: Path,
            arguments: List[String],
            classpathURLs: List[URL],
            runs: Int,
            multiplier: Int,
            sampleNumber: Int,
            shouldCompile: Boolean,
            config: Config): Benchmark =
    new SnippetBenchmark(name, src, arguments, classpathURLs, runs, multiplier, sampleNumber, shouldCompile, config)

  def apply(xml: Elem, config: Config): Benchmark = scala.xml.Utility.trim(xml) match {
    case <Benchmark><name>{ name }</name><src>{ src }</src><arguments>{ argumentsNode@_* }</arguments><classpath>{ classpathURLsNode@_* }</classpath><runs>{ runs }</runs><multiplier>{ multiplier }</multiplier><sampleNumber>{ sampleNumber }</sampleNumber><shouldCompile>{ shouldCompile }</shouldCompile></Benchmark> =>
      val arguments = for (arg <- argumentsNode) yield arg.text
      val classpathURLs = for (cp <- classpathURLsNode) yield Path(cp.text).toURL
      this(
        name.text,
        Path(src.text),
        arguments.toList,
        classpathURLs.toList,
        runs.text.toInt,
        multiplier.text.toInt,
        sampleNumber.text.toInt,
        shouldCompile.text.toBoolean,
        config)
    case _ => null
  }

}
