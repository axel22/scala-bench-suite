/*
 * PinpointBenchmarkSnippet
 * 
 * Version
 * 
 * Created on October 31st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import java.lang.reflect.Method
import java.net.URL

import scala.collection.immutable.List
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.Path
import scala.tools.sbs.performance.PerformanceBenchmarkSnippet

class PinpointBenchmarkSnippet(name: String,
                               arguments: List[String],
                               classpathURLs: List[URL],
                               src: Path,
                               sampleNumber: Int,
                               multiplier: Int,
                               measurement: Int,
                               val pinpointClass: String,
                               val pinpointMethod: String,
                               val pinpointExclude: List[String],
                               val pinpointPrevious: Directory,
                               method: Method,
                               context: ClassLoader,
                               config: Config)
  extends PerformanceBenchmarkSnippet(
    name,
    arguments,
    classpathURLs,
    src,
    sampleNumber,
    multiplier,
    measurement,
    method,
    context,
    config)
  with PinpointBenchmark

object PinpointBenchmarkSnippet {

  def apply(name: String,
            arguments: List[String],
            classpathURLs: List[URL],
            src: Path,
            sampleNumber: Int,
            multiplier: Int,
            measurement: Int,
            pinpointClass: String,
            pinpointMethod: String,
            pinpointExclude: List[String],
            pinpointPrevious: Directory,
            method: Method,
            context: ClassLoader,
            config: Config): PinpointBenchmarkSnippet =
    new PinpointBenchmarkSnippet(
      name,
      arguments,
      classpathURLs,
      src,
      sampleNumber,
      multiplier,
      measurement,
      pinpointClass,
      pinpointMethod,
      pinpointExclude,
      pinpointPrevious,
      method,
      context,
      config)

  def unapply(snippet: PinpointBenchmarkSnippet): Option[(String, List[String], List[URL], Path, Int, Int, Int, String, String, List[String], Directory, Method, ClassLoader, Config)] =
    Some(
      snippet.name,
      snippet.arguments,
      snippet.classpathURLs,
      snippet.src,
      snippet.sampleNumber,
      snippet.multiplier,
      snippet.measurement,
      snippet.pinpointClass,
      snippet.pinpointMethod,
      snippet.pinpointExclude,
      snippet.pinpointPrevious,
      snippet.method,
      snippet.context,
      snippet.config)

}
