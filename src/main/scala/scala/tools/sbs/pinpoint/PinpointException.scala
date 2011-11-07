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

import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.InstrumentingExpression
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.util.Constant

class PinpointException(message: String) extends BenchmarkException(message)

case class PinpointingMethodNotFoundException(benchmark: PinpointBenchmark)
  extends PinpointException(
    "Pinpointing method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod +
      " not found in " + benchmark.pinpointClass)

case class MismatchExpressionList(benchmark: PinpointBenchmark,
                                  currentCallingList: List[MethodCallExpression],
                                  previousCallingList: List[MethodCallExpression])
  extends PinpointException("Mismatching expression list: " + Constant.ENDL +
    (currentCallingList map (c => c.getClassName + "." + c.getMethodName) mkString " ") + Constant.ENDL +
    (previousCallingList map (c => c.getClassName + "." + c.getMethodName) mkString " ") + Constant.ENDL +
    "in method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod)

case class BottleneckUndetectableException(benchmark: PinpointBenchmark, callingList: List[InstrumentingExpression])
  extends PinpointException("Measurement failure " + (
    if (callingList == Nil) ""
    else "from line " + callingList.head.getLineNumber +
      " to line " + callingList.last.getLineNumber +
      " in method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod) +
    " from benchmark " + benchmark.name)

case class NoPinpointingMethodException(benchmark: PinpointBenchmark)
  extends PinpointException("No pinpointing method specified in " + benchmark.name)

class ANOVAUnsupportedException extends PinpointException("Currently ANOVA test is unsupported")

case class UninstrumentableException(className: String)
  extends PinpointException("Cannot instrument class " + className)
