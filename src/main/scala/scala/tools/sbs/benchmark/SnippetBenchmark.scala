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
import scala.sys.process.Process
import scala.sys.process.ProcessBuilder
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.util.Constant.COLON
import scala.tools.sbs.BenchmarkMode
import scala.tools.sbs.Config
import scala.tools.nsc.util.ClassPath

/** An implement of {@link Benchmark} trait.
 *  `method` is the `main(args: Array[String])` method of the benchmark `object`.
 */
case class SnippetBenchmark(name: String,
                            arguments: List[String],
                            classpathURLs: List[URL],
                            runs: Int,
                            multiplier: Int,
                            sampleNumber: Int,
                            method: Method,
                            newContext: ClassLoader,
                            profiledClasses: List[String],
                            excludeClasses: List[String],
                            profiledMethod: String,
                            profiledField: String,
                            config: Config) extends Benchmark {

  /** Benchmark process.
   */
  private var process: ProcessBuilder = null

  def createLog(mode: BenchmarkMode): Log = LogFactory(name, mode, config)

  /** Current class loader context.
   */
  private val oldContext = Thread.currentThread.getContextClassLoader

  /** Sets the running context and load benchmark classes.
   */
  def init() = Thread.currentThread.setContextClassLoader(newContext)

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run() = method.invoke(null, Array(arguments.toArray: AnyRef): _*)

  /** Resets the context.
   */
  def reset() = Thread.currentThread.setContextClassLoader(oldContext)

  def toXML =
    <SnippetBenchmark>
      <name>{ name }</name>
      <arguments>{ for (arg <- arguments) yield <arg>{ arg }</arg> }</arguments>
      <classpath>{ for (cp <- classpathURLs) yield <cp> { cp.getPath } </cp> }</classpath>
      <runs>{ runs.toString }</runs>
      <multiplier>{ multiplier.toString }</multiplier>
    </SnippetBenchmark>

}
