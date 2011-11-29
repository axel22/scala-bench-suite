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
package bottleneck

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.performance.regression.CIRegression
import scala.tools.sbs.pinpoint.instrumentation.CodeInstrumentor.MethodCallExpression

abstract class BottleneckFound(benchmark: Benchmark) extends ScrutinyResult {

  def benchmarkName = benchmark.name

}

case class Bottleneck(benchmark: Benchmark,
                      position: InvocationGraph,
                      current: (Double, Double),
                      previous: ArrayBuffer[(Double, Double)],
                      CI: (Double, Double))
  extends BottleneckFound(benchmark)
  with CIRegression
  with ScrutinySuccess {

  override def toReport = {
    ArrayBuffer(
      "Bottleneck found:",
      "  from method call " +
        position.first.prototype + " at the " + position.startOrdinum + " time of its invocations") ++
      (if (position.length > 1)
        ArrayBuffer("  to method call " +
        position.last.prototype + " at the " + position.endOrdinum, " time of its invocations")
      else Nil) ++
      super.toReport
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

  override def toReport = ArrayBuffer("No bottleneck found", "") ++ super.toReport

}
