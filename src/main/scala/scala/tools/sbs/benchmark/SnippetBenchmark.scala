/*
 * SnippetBenchmark
 * 
 * Version 
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import java.lang.reflect.Method
import java.lang.Thread
import java.net.URL

import scala.sys.process.ProcessBuilder
import scala.tools.nsc.io.Path
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory

/** An implement of {@link Benchmark} trait.
 *  `method` is the `main(args: Array[String])` method of the benchmark `object`.
 */
abstract class SnippetBenchmark(_name: String,
                                _arguments: List[String],
                                _classpathURLs: List[URL],
                                _src: Path,
                                _sampleNumber: Int,
                                _timeout: Int,
                                method: Method,
                                _context: ClassLoader,
                                config: Config) extends Benchmark {

  def name = _name

  def arguments = _arguments

  def classpathURLs = _classpathURLs

  def src = _src

  def sampleNumber = _sampleNumber

  def context = _context

  def timeout = _timeout

  /** Benchmark process.
   */
  private var process: ProcessBuilder = null

  /** Current class loader context.
   */
  private val oldContext = Thread.currentThread.getContextClassLoader

  /** Sets the running context and load benchmark classes.
   */
  def init() = Thread.currentThread.setContextClassLoader(context)

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run() = method.invoke(null, Array(arguments.toArray: AnyRef): _*)

  /** Resets the context.
   */
  def reset() = Thread.currentThread.setContextClassLoader(oldContext)

  def createLog(mode: BenchmarkMode): Log = LogFactory(name, mode, config)

  def toXML =
    <SnippetBenchmark>
      <name>{ name }</name>
      <arguments>{ for (arg <- arguments) yield <arg>{ arg }</arg> }</arguments>
      <classpath>{ for (cp <- classpathURLs) yield <cp> { cp.getPath } </cp> }</classpath>
      <src>{ src.path }</src>
    </SnippetBenchmark>

}
