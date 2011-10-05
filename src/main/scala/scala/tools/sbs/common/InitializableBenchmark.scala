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
package common

import java.lang.ClassNotFoundException
import java.lang.InstantiationException
import java.lang.Thread
import java.net.URL

import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory
import java.lang.Thread.sleep

/** Represents benchmarks that have to be initialized before performance check.
 */
case class InitializableBenchmark(name: String,
                                  src: Path,
                                  classpathURLs: List[URL],
                                  shouldCompile: Boolean,
                                  config: Config) extends Benchmark {

  val arguments = List[String]()

  lazy val runs = benchmarkObject.runs

  lazy val multiplier = benchmarkObject.multiplier

  lazy val sampleNumber = benchmarkObject.sampleNumber

  def init = benchmarkObject.init

  def run = benchmarkObject.run

  def reset = ()

  /** The actual benchmark loaded using reflection.
   *  `lazy` guaratees that the object is not loaded before compiled.
   */
  private lazy val benchmarkObject: BenchmarkTemplate = try {
    val classLoader = ScalaClassLoader.fromURLs(
      config.classpathURLs ++ classpathURLs, classOf[BenchmarkTemplate].getClassLoader)
    val clazz = classLoader.tryToInitializeClass(name) getOrElse (throw new ClassNotFoundException(name))
    Thread.currentThread.setContextClassLoader(classLoader)
    clazz.asInstanceOf[Class[_]].newInstance.asInstanceOf[BenchmarkTemplate]
  } catch {
    case x: ClassNotFoundException => throw new ClassNotFoundException(
      name + " args = " + (arguments mkString ", ") + ", classpath = " + ClassPath.fromURLs(classpathURLs: _*))
    case i: InstantiationException => throw new ClassNotFoundException(name + " should not be an object")
  }

  def createLog(mode: BenchmarkMode): Log = LogFactory(name, mode, config)

  def initCommand(): Boolean = false

  /** Runs the benchmark process.
   */
  def runCommand = ()

  def toXML =
    <InitializableBenchmark>
      <name>{ name }</name>
      <src>{ src }</src>
      <classpath>{ for (cp <- classpathURLs) yield <cp> { cp.getPath } </cp> }</classpath>
    </InitializableBenchmark>

}
