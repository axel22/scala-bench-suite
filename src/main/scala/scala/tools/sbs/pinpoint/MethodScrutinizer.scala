/*
 * MethodScrutinizer
 * 
 * Version
 * 
 * Created on October 13th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.Config

class MethodScrutinizer(config: Config) extends Scrutinizer {

  def scrutinize(benchmark: Benchmark): ScrutinyResult = {
    log = benchmark createLog Pinpointing
    if (benchmark.pinpointClass == "" || benchmark.pinpointMethod == "") {
      throw new Exception("No pinpointing method specified")
    }
    instrument(benchmark)
    MeasurerFactory(config, Pinpointing) measure benchmark
  }

  /** Modifies the `pinpointMethod` to set entry and exit time to
   *  {@link scala.tools.sbs.pinpoint.PinpointHarness}'s static fields.
   */
  private def instrument(benchmark: Benchmark) {
    val instrumentor = CodeInstrumentor(config)
    val (clazz, method) = instrumentor.getClassAndMethod(
      benchmark.pinpointClass,
      benchmark.pinpointMethod,
      config.classpathURLs ++ benchmark.classpathURLs)
    if (method == null) {
      throw new Exception("Cannot find method " + benchmark.pinpointMethod)
    }
    instrumentor.sandwich(method, PinpointHarness.javaInstructionCallStart, PinpointHarness.javaInstructionCallEnd)
    instrumentor.writeFile(clazz, benchmark.context)
  }

}
