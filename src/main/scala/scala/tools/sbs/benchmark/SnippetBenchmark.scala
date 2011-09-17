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
import java.lang.reflect.Modifier
import java.net.URL

import scala.sys.process.Process
import scala.sys.process.ProcessBuilder
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

import BenchmarkMode.BenchmarkMode

case class SnippetBenchmark(name: String,
                     arguments: List[String],
                     modes: List[BenchmarkMode],
                     classpathURLs: List[URL],
                     src: List[File],
                     bin: Directory,
                     log: Log,
                     config: Config) extends Benchmark {

  /**
   * Benchmark process.
   */
  private var process: ProcessBuilder = null

  /**
   * Benchmark `main` method.
   */
  private var method: Method = null

  /**
   * Current class loader context.
   */
  private val oldContext = Thread.currentThread.getContextClassLoader

  /**
   * Uses strange named compiler Global to compile.
   */
  def compile(): Boolean = {
    log.verbose("[Compile]")

    val settings = new Settings(log.error)
    val (ok, errArgs) = settings.processArguments(
      List(
        "-classpath",
        (classpathURLs map (_.toString) mkString (System getProperty "path.separator")),
        "-d",
        bin.path),
      false)

    log.debug(settings.d.value)
    settings.outdir.value = bin.path

    if (ok) {
      val compiler = new Global(settings)
      (new compiler.Run) compile (src map (_.path))
    } else {
      errArgs map (err => log.error(err))
    }
    ok
  }

  /**
   * Sets the running context and load benchmark classes.
   */
  def init() {
    try {
      val classLoader = (ScalaClassLoader fromURLs classpathURLs)
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

  /**
   * Runs the benchmark object and throws Exceptions (if any).
   */
  def run() = method.invoke(null, Array(arguments.toArray: AnyRef): _*)

  /**
   * Resets the context.
   */
  def finallize() = Thread.currentThread.setContextClassLoader(oldContext)

  /**
   * Creates the process command for start up benchmarking.
   */
  def initCommand(): Boolean = {
    val colon = System getProperty "path.separator"
    val command = arguments.foldLeft(
      Seq(config.JAVACMD,
        "-cp",
        config.SCALALIB,
        config.JAVAPROP,
        "scala.tools.nsc.MainGenericRunner",
        "-classpath",
        bin.path + colon + config.SCALALIB + colon + (classpathURLs map (_.toString) mkString colon),
        name)
    ) { (cmd, arg) => cmd :+ arg }

    log.debug(command.toString)

    process = Process(command)

    // Ignore the first launch due to system status changing
    process.! == 0
  }

  /**
   * Runs the benchmark process.
   */
  def runCommand() = process !

  override def toString = "Benchmark [" + name + "] [" + arguments mkString " "

}
