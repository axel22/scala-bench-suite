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
package common

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.ClassNotFoundException
import java.lang.NoSuchMethodException
import java.lang.Thread
import java.net.URL

import scala.sys.process.Process
import scala.sys.process.ProcessBuilder
import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.LogFactory
import scala.tools.sbs.util.Constant.COLON

/** An implement of {@link Benchmark} trait.
 */
case class SnippetBenchmark(name: String,
                            src: Path,
                            arguments: List[String],
                            classpathURLs: List[URL],
                            runs: Int,
                            multiplier: Int,
                            sampleNumber: Int,
                            shouldCompile: Boolean,
                            config: Config) extends Benchmark {

  /** Benchmark process.
   */
  private var process: ProcessBuilder = null

  /** Benchmark `main` method.
   */
  private var method: Method = null

  /** Current class loader context.
   */
  private val oldContext = Thread.currentThread.getContextClassLoader

  def createLog(mode: BenchmarkMode): Log = LogFactory(name, mode, config)
  
  /** Sets the running context and load benchmark classes.
   */
  def init() {
    try {
      val classLoader = ScalaClassLoader fromURLs (config.classpathURLs ++ classpathURLs)
      val clazz = classLoader.tryToInitializeClass(name) getOrElse (throw new ClassNotFoundException(name))
      method = clazz.getMethod("main", classOf[Array[String]])
      if (!Modifier.isStatic(method.getModifiers)) {
        throw new NoSuchMethodException(name + ".main is not static")
      }
      Thread.currentThread.setContextClassLoader(classLoader)
    } catch {
      case x: ClassNotFoundException => throw new ClassNotFoundException(
        name + " args = " + (arguments mkString ", ") + ", classpath = " + ClassPath.fromURLs(classpathURLs: _*))
    }
  }

  /** Runs the benchmark object and throws Exceptions (if any).
   */
  def run() = method.invoke(null, Array(arguments.toArray: AnyRef): _*)

  /** Resets the context.
   */
  def reset() = Thread.currentThread.setContextClassLoader(oldContext)

  /** Creates the process command for start up benchmarking.
   */
  def initCommand(): Boolean = {
    val command = arguments.foldLeft(
      Seq(config.javacmd,
        "-cp",
        config.scalaLib,
        config.javaProp,
        "scala.tools.nsc.MainGenericRunner",
        "-classpath",
        config.bin.path + COLON + config.scalaLib + COLON + (classpathURLs map (_.toString) mkString COLON),
        name))((cmd, arg) => cmd :+ arg)

    process = Process(command)

    // Ignore the first launch due to system status changing
    process.! == 0
  }

  /** Runs the benchmark process.
   */
  def runCommand() = process !

  def toXML =
    <SnippetBenchmark>
      <name>{ name }</name>
      <src>{ src.path }</src>
      <arguments>{ for (arg <- arguments) yield <arg>{ arg }</arg> }</arguments>
      <classpath>{ for (cp <- classpathURLs) yield <cp> { cp.getPath } </cp> }</classpath>
      <runs>{ runs.toString }</runs>
      <multiplier>{ multiplier.toString }</multiplier>
      <sampleNumber>{ sampleNumber.toString }</sampleNumber>
      <shouldCompile>{ shouldCompile.toString }</shouldCompile>
    </SnippetBenchmark>

}
