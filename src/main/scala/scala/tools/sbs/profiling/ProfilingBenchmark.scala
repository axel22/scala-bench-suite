/*
 * ProfilingBenchmark
 * 
 * Version
 * 
 * Created on October 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import java.lang.reflect.Method

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log
import scala.tools.sbs.util.Constant

trait ProfilingBenchmark extends Benchmark {

  /** Names of the classes to be profiled the loading.
   */
  def profileClasses: List[String]

  /** Names of the classes to be ignored from profiling.
   */
  def profileExclude: List[String]

  /** Name of the method to be profiled the invocations.
   */
  def profileMethod: String

  /** Name of the field to be profiled the accessing and modifying.
   */
  def profileField: String

}

class ProfilingBenchmarkFactory(protected val log: Log, protected val config: Config) extends BenchmarkFactory {

  protected val profileClassOpt = "--profile-class"

  protected val profileExcludeOpt = "--profile-exclude"

  protected val profileMethodOpt = "--profile-method"

  protected val profileFieldOpt = "--profile-field"

  def createFrom(info: BenchmarkInfo): Benchmark = {
    val argMap = BenchmarkInfo.readInfo(
      info.src,
      List(profileClassOpt, profileExcludeOpt, profileMethodOpt, profileFieldOpt))
    val profileClass = argMap get profileClassOpt match {
      case Some(arg) => arg split Constant.COLON toList
      case _         => config.profileClasses
    }
    val profileExclude = argMap get profileExcludeOpt match {
      case Some(arg) => arg split Constant.COLON toList
      case _         => config.profileExclude
    }
    val profileMethod = argMap get profileMethodOpt match {
      case Some(arg) => arg
      case _         => config.profileMethod
    }
    val profileField = argMap get profileFieldOpt match {
      case Some(arg) => arg
      case _         => config.profileField
    }
    load(
      info,
      (method: Method, context: ClassLoader) => new ProfilingBenchmarkSnippet(
        info.name,
        info.arguments,
        info.classpathURLs,
        info.src,
        info.sampleNumber,
        info.timeout,
        profileClass,
        profileExclude,
        profileMethod,
        profileField,
        method,
        context,
        config),
      (context: ClassLoader) => new ProfilingBenchmarkInitializable(
        info.name,
        info.classpathURLs,
        info.src,
        Reflector(config, log).getObject[ProfilingBenchmarkTemplate](info.name, config.classpathURLs ++ info.classpathURLs),
        context,
        config))
  }

}

