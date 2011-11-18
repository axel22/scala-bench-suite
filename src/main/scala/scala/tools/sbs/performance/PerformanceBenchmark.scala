
/*
 * PerformanceBenchmark
 * 
 * Version
 * 
 * Created on October 29th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance

import java.lang.reflect.Method

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log

trait PerformanceBenchmark extends Benchmark {

  def multiplier: Int

  def measurement: Int

}

class PerformanceBenchmarkFactory(protected val log: Log, protected val config: Config) extends BenchmarkFactory {

  protected val multiplierOpt = "--multiplier"

  protected val measurementOpt = "--measurement"

  /** Creates a `Benchmark` from the given arguments.
   */
  def createFrom(info: BenchmarkInfo): Benchmark = {
    val argMap = BenchmarkInfo.readInfo(info.src, List(multiplierOpt, measurementOpt))
    val multiplier = argMap get multiplierOpt match {
      case Some(arg) => arg.toInt
      case _         => config.multiplier
    }
    val measurement = argMap get measurementOpt match {
      case Some(arg) => arg.toInt
      case _         => config.measurement
    }
    load(
      info,
      (method: Method, context: ClassLoader) => new PerformanceBenchmarkSnippet(
        info.name,
        info.arguments,
        info.classpathURLs,
        info.src,
        info.sampleNumber,
        info.timeout,
        multiplier,
        measurement,
        method,
        context,
        config),
      (context: ClassLoader) => new PerformanceBenchmarkInitializable(
        info.name,
        info.classpathURLs,
        info.src,
        Reflector(config, log).getObject[PerformanceBenchmarkTemplate](
          info.name, config.classpathURLs ++ info.classpathURLs),
        context,
        config))
  }

}
