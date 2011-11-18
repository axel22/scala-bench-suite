/*
 * InitializableBenchmark
 * 
 * Version
 * 
 * Created on October 4th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import java.lang.Thread
import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory

/** Represents benchmarks that have to be initialized before performance check.
 *  `benchmarkObject`: The actual benchmark loaded using reflection.
 */
abstract case class InitializableBenchmark(name: String,
                                           classpathURLs: List[URL],
                                           src: Path,
                                           benchmarkObject: BenchmarkTemplate,
                                           context: ClassLoader,
                                           config: Config) extends Benchmark {

  val arguments = List[String]()

  val sampleNumber = benchmarkObject.sampleNumber

  /** Current class loader context.
   */
  private val oldContext = Thread.currentThread().getContextClassLoader()

  def timeout = benchmarkObject.timeout

  def init() = {
    Thread.currentThread.setContextClassLoader(context)
    benchmarkObject.init
  }

  def run() = benchmarkObject.run

  def reset() = {
    Thread.currentThread.setContextClassLoader(oldContext)
    benchmarkObject.reset
  }

  def createLog(mode: BenchmarkMode): Log = LogFactory(name, mode, config)

  def toXML =
    <InitializableBenchmark>
      <name>{ name }</name>
      <classpath>{ for (cp <- classpathURLs) yield <cp> { cp.getPath } </cp> }</classpath>
      <src>{ src.path }</src>
    </InitializableBenchmark>

}
