/*
 * ScrutinyRegressionResult
 * 
 * Version
 * 
 * Created on November 1st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.pinpoint.CodeInstrumentor.MethodCallExpression
import scala.tools.sbs.regression.CIRegression
import scala.tools.sbs.util.Constant

abstract class BottleneckFound(benchmark: Benchmark) extends ScrutinyResult {

  def benchmarkName = benchmark.name

}

case class Bottleneck(benchmark: Benchmark,
                      position: List[MethodCallExpression],
                      current: (Double, Double),
                      previous: ArrayBuffer[(Double, Double)],
                      CI: (Double, Double))
  extends BottleneckFound(benchmark)
  with CIRegression
  with ScrutinySuccess {

  override def toReport = {
    val to =
      if (position.length > 1)
        ArrayBuffer(Constant.INDENT + "  to method call " +
          position.last.getClassName + "." + position.last.getMethodName +
          " at line " + position.last.getLineNumber, "")
      else ArrayBuffer("")
    ArrayBuffer(
      Constant.INDENT + "Bottleneck found:",
      Constant.INDENT + "  from method call " +
        position.head.getClassName + "." + position.head.getMethodName +
        " at line " + position.head.getLineNumber) ++ to ++ super.toReport
  }

}

case class NoBottleneck(benchmark: Benchmark,
                        confidenceLevel: Int,
                        current: (Double, Double),
                        previous: ArrayBuffer[(Double, Double)],
                        CI: (Double, Double))
  extends BottleneckFound(benchmark)
  with CIRegression
  with ScrutinyFailure {

  override def toReport = ArrayBuffer(Constant.INDENT + "No bottleneck found.", "") ++ super.toReport

}
