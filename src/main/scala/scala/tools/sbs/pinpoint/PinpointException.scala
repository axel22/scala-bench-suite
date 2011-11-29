/*
 * PinpointException
 * 
 * Version
 * 
 * Created on November 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.pinpoint.bottleneck.InvocationCollector
import scala.tools.sbs.pinpoint.bottleneck.InvocationGraph
import scala.tools.sbs.util.Constant

class PinpointException(message: String) extends BenchmarkException(message)

case class PinpointingMethodNotFoundException(benchmark: PinpointBenchmark)
  extends PinpointException(
    "Pinpointing method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod +
      " not found in " + benchmark.pinpointClass)

case class MismatchExpressionList(declaringClass: String,
                                  method: String,
                                  collector: InvocationCollector)
  extends PinpointException("Mismatching expression list: " + Constant.ENDL +
    (collector.currentGraph traverse (_ prototype) mkString " -> ") + Constant.ENDL +
    (collector.previousGraph traverse (_ prototype) mkString " -> ") + Constant.ENDL +
    "in method " + declaringClass + "." + method)

case class BottleneckUndetectableException(declaringClass: String,
                                           method: String,
                                           graph: InvocationGraph)
  extends PinpointException("Measurement failure " + (
    if (graph.length == 0) ""
    else "from method call " + graph.first.prototype + " at the " + graph.startOrdinum + " time of its invocations" +
      " to method call " + graph.last.prototype + " at the " + graph.endOrdinum + " time of its invocations" +
      " in method " + declaringClass + "." + method))

case class NoPinpointingMethodException(benchmark: PinpointBenchmark)
  extends PinpointException("No pinpointing method specified in " + benchmark.name)

class ANOVAUnsupportedException extends PinpointException("Currently ANOVA test is unsupported")

case class UninstrumentableException(className: String)
  extends PinpointException("Cannot instrument class " + className)
