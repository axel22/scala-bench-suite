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

import java.lang.reflect.Modifier
import scala.tools.nsc.io.Path
import scala.tools.nsc.util.ClassPath
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.Config
import scala.xml.Elem

/** Factory object used to create a benchmark entity.
 */
object BenchmarkFactory {

  /** Creates a `Benchmark` from the given arguments.
   */
  def apply(info: BenchmarkInfo, config: Config): Benchmark = {
    val classpathURLs = config.classpathURLs ++ info.classpathURLs
    try {
      val (clazz, context) = Reflector(config).getClass(info.name, classpathURLs)
      try {
        val method = clazz.getMethod("main", classOf[Array[String]])
        if (!Modifier.isStatic(method.getModifiers)) {
          throw new NoSuchMethodException(info.name + ".main is not static")
        }
        new SnippetBenchmark(
          info.name,
          info.arguments,
          info.classpathURLs,
          info.runs,
          info.multiplier,
          info.sampleNumber,
          method,
          context,
          info.profiledClasses,
          info.excludeClasses,
          info.profiledMethod,
          info.profiledField,
          info.pinpointClass,
          info.pinpointMethod,
          config)
      }
      catch {
        case s: NoSuchMethodException => try {
          new InitializableBenchmark(
            info.name,
            info.classpathURLs,
            Reflector(config).getObject[BenchmarkTemplate](info.name, classpathURLs)._1,
            context,
            info.profiledClasses: List[String],
            info.excludeClasses: List[String],
            info.profiledMethod,
            info.profiledField,
            info.pinpointClass,
            info.pinpointMethod,
            config)
        }
        catch {
          case c: ClassCastException =>
            throw new ClassCastException(info.name + " should implement scala.tools.sbs.benchmark.BenchmarkTemplate")
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
  def apply(xml: Elem, config: Config): Benchmark = try {
    val name = (xml \\ "name").text
    val classpathURLs = (xml \\ "cp") map (cp => Path(cp.text).toURL) toList
    val pinpointClass = (xml \\ "pinpointClass").text
    val pinpointMethod = (xml \\ "pinpointMethod").text

    if ((xml \\ "SnippetBenchmark").length == 1) {
      this(
        new BenchmarkInfo(
          name,
          null,
          (xml \\ "arg") map (_.text) toList,
          classpathURLs,
          (xml \\ "runs").text.toInt,
          (xml \\ "multiplier").text.toInt,
          0,
          false,
          Nil,
          Nil,
          "",
          "",
          pinpointClass,
          pinpointMethod),
        config)
    }
    else if ((xml \\ "InitializableBenchmark").length == 1) {
      this(
        new BenchmarkInfo(
          name,
          null,
          Nil,
          classpathURLs,
          0,
          0,
          0,
          false,
          Nil,
          Nil,
          "",
          "",
          pinpointClass,
          pinpointMethod),
        config)
    }
    else {
      throw new Exception("Benchmark xml is not as expected")
    }
  }
  catch {
    case _ => throw new Exception("Getting benchmark from super process failed")
  }

}
