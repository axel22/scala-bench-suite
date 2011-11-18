/*
 * PinpointingBenchmark
 * 
 * Version
 * 
 * Created on October 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import java.lang.reflect.Method

import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.PerformanceBenchmark
import scala.tools.sbs.performance.PerformanceBenchmarkFactory
import scala.tools.sbs.util.Constant

trait PinpointBenchmark extends PerformanceBenchmark {

  /** Name of the class to be pinpointing regression detected.
   */
  def pinpointClass: String

  /** Name of the method to be pinpointing regression detected.
   */
  def pinpointMethod: String

  /** Names of the classes to be ignored during pinpointing regression detection.
   */
  def pinpointExclude: List[String]

  /** Location of the old class files to be used during pinpointing regression detection.
   *  Should not be included in `Config.classpathURLs` and `Benchmark.classpathURLs`.
   */
  def pinpointPrevious: Directory

  /** Maximum recursion depth in the process of finding bottleneck.
   *  Value of -1 is stand for unlimited depth.
   */
  def pinpointDepth: Int

}

class PinpointBenchmarkFactory(log: Log, config: Config) extends PerformanceBenchmarkFactory(log, config) {

  protected val pinpointClassOpt = "--pinpoint-class"

  protected val pinpointMethodOpt = "--pinpoint-method"

  protected val pinpointPreviousOpt = "--pinpoint-previous"

  protected val pinpointExcludeOpt = "--pinpoint-exclude"

  protected val pinpointDepthOpt = "--pinpoint-depth"

  override def createFrom(info: BenchmarkInfo): Benchmark = {
    val argMap = BenchmarkInfo.readInfo(
      info.src,
      List(
        multiplierOpt,
        measurementOpt,
        pinpointClassOpt,
        pinpointMethodOpt,
        pinpointExcludeOpt,
        pinpointPreviousOpt,
        pinpointDepthOpt))
    val multiplier = argMap get multiplierOpt match {
      case Some(arg) => arg.toInt
      case _         => config.multiplier
    }
    val measurement = argMap get measurementOpt match {
      case Some(arg) => arg.toInt
      case _         => config.measurement
    }
    val pinpointClass = argMap get pinpointClassOpt match {
      case Some(arg) => arg
      case _         => config.pinpointClass
    }
    val pinpointMethod = argMap get pinpointMethodOpt match {
      case Some(arg) => arg
      case _         => config.pinpointMethod
    }
    val pinpointExclude = argMap get pinpointExcludeOpt match {
      case Some(arg) => arg split Constant.COLON toList
      case _         => config.pinpointExclude
    }
    val pinpointPrevious = argMap get pinpointPreviousOpt match {
      case Some(arg) => Directory(arg)
      case _         => config.pinpointPrevious
    }
    val pinpointDepth = argMap get pinpointPreviousOpt match {
      case Some(arg) => arg.toInt
      case _         => -1
    }
    load(
      info,
      (method: Method, context: ClassLoader) => new PinpointBenchmarkSnippet(
        info.name,
        info.arguments,
        info.classpathURLs,
        info.src,
        info.sampleNumber,
        info.timeout,
        multiplier,
        measurement,
        pinpointClass,
        pinpointMethod,
        pinpointExclude,
        pinpointPrevious,
        pinpointDepth,
        method,
        context,
        config),
      (context: ClassLoader) => new PinpointBenchmarkInitializable(
        info.name,
        info.classpathURLs,
        info.src,
        Reflector(config, log).getObject[PinpointBenchmarkTemplate](info.name, config.classpathURLs ++ info.classpathURLs),
        context,
        config))
  }

}
