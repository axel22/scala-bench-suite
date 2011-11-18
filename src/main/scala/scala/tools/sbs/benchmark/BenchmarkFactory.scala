/*
 * BenchmarkFactory
 * 
 * Version
 * 
 * Created on October 8th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import java.lang.reflect.Method
import java.lang.reflect.Modifier

import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.performance.PerformanceBenchmarkFactory
import scala.tools.sbs.pinpoint.PinpointBenchmarkFactory
import scala.tools.sbs.profiling.ProfilingBenchmarkFactory
import scala.xml.Elem

/** Factory object used to create a benchmark entity.
 */
trait BenchmarkFactory {

  protected def log: Log

  protected def config: Config

  def createFrom(info: BenchmarkInfo): Benchmark

  /** Creates a `Benchmark` from the given arguments.
   */
  protected def load(info: BenchmarkInfo,
                     newSnippet: (Method, ClassLoader) => SnippetBenchmark,
                     newInitializable: ClassLoader => InitializableBenchmark): Benchmark = {
    val classpathURLs = config.classpathURLs ++ info.classpathURLs
    try {
      val clazz = Reflector(config, log).getClass(info.name, classpathURLs)
      try {
        val method = clazz.getMethod("main", classOf[Array[String]])
        if (!Modifier.isStatic(method.getModifiers)) {
          throw new NoSuchMethodException(info.name + ".main is not static")
        }
        log.debug("Snippet benchmark: " + info.name)
        UI.debug("Snippet benchmark: " + info.name)
        newSnippet(method, clazz.getClassLoader)
      }
      catch {
        case _: NoSuchMethodException => try {
          log.debug("Initializable benchmark: " + info.name)
          UI.debug("Initializable benchmark: " + info.name)
          newInitializable(clazz.getClassLoader)
        }
        catch {
          case _: ClassCastException => throw new ClassCastException(
            info.name + " should implement scala.tools.sbs.benchmark.BenchmarkTemplate")
          case _: ClassNotFoundException => throw new ClassNotFoundException(
            info.name + " should be an object or a class (not trait nor abstract)")
        }
      }
    }
    catch {
      case _: ClassNotFoundException =>
        throw new ClassNotFoundException(
          info.name + " classpath = " + ClassPath.fromURLs(classpathURLs: _*))
    }
  }

  /** Creates a `Benchmark` from a xml element representing it.
   */
  def createFrom(xml: Elem): Benchmark =
    try createFrom(
      new BenchmarkInfo(
        (xml \\ "name").text,
        Path(xml \\ "src" text),
        (xml \\ "arg") map (_.text) toList,
        (xml \\ "cp") map (cp => Path(cp.text).toURL) toList,
        0,
        0,
        false))
    catch {
      case c: ClassCastException => {
        log.error(c.toString)
        throw c
      }
      case c: ClassNotFoundException => {
        log.error(c.toString)
        throw c
      }
      case e => {
        log.error(e.toString)
        throw new Exception("Getting benchmark from super process failed")
      }
    }

}

object BenchmarkFactory {

  def apply(log: Log, config: Config, mode: BenchmarkMode): BenchmarkFactory = mode match {
    case Profiling   => new ProfilingBenchmarkFactory(log, config)
    case Pinpointing => new PinpointBenchmarkFactory(log, config)
    case _           => new PerformanceBenchmarkFactory(log, config)
  }

}
