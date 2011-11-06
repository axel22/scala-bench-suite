/*
 * InstrumentingBenchmark
 * 
 * Version 
 * 
 * Created on November 6th, 2011
 * 
 * Created by PDB
 */

package scala.tools.sbs
package instrumenting

import java.lang.reflect.Method
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkFactory
import scala.tools.sbs.benchmark.BenchmarkInfo
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.util.Constant
import scala.tools.sbs.io.Log

trait InstrumentingBenchmark extends Benchmark {
  /** List of name of methods to be instrumented the invocations.
   */
  def instrumentMethods: List[String]

}

class InstrumentingBenchmarkFactory(protected val log: Log, protected val config: Config) extends BenchmarkFactory {
  protected val instrumentMethodOpt = "--instrument-methods"

  def createFrom(info: BenchmarkInfo): Benchmark = {
    val argMap = BenchmarkInfo.readInfo(
      info.src,
      List(instrumentMethodOpt))
    val instrumentMethod = argMap get instrumentMethodOpt match {
      case Some(arg) => arg split Constant.COLON toList
      case _         => config.instrumentMethods
    }
    load(
      info,
      (method: Method, context: ClassLoader) => new InstrumentingBenchmarkSnippet(
        info.name,
        info.arguments,
        info.classpathURLs,
        info.src,
        info.sampleNumber,
        instrumentMethod,
        method,
        context,
        config),
      (context: ClassLoader) => new InstrumentingBenchmarkInitializable(
        info.name,
        info.classpathURLs,
        info.src,
        Reflector(config).getObject[InstrumentingBenchmarkTemplate](info.name, config.classpathURLs ++ info.classpathURLs),
        context,
        config))
  }
}