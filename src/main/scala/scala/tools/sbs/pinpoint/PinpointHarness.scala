/*
 * ScrutinyType
 * 
 * Version
 * 
 * Created on October 16th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.SubProcessMeasurer
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.util.Constant.STEADY_THRESHOLD
import scala.collection.mutable.Stack

/** Measurer for pinpointing regression detection.
 *  Runs just like {@link scala.tools.sbs.measurement.SteadyHarness}
 *  does, but only measures the first running time of a piece of code.
 */
object PinpointHarness extends SubProcessMeasurer {
  
  val javaInstructionCallStart = getClass.getName.replace("$", "") + ".start(" + javaExpressionCurrentTime + ");"
  val javaInstructionCallEnd = getClass.getName.replace("$", "") + ".end(" + javaExpressionCurrentTime + ");"
  private def javaExpressionCurrentTime = "System.currentTimeMillis()"

  /** Set only once at the first time measured.
   */
  private var measured = -1L

  /** Used to detect whether back to the first run of the recursion.
   */
  private var recursionDepth = 0

  /** Set only once as the first entry of the observed piece of code.
   */
  private var firstTimeStone = 0L

  /** Sets values at the time enter the observed piece of code.
   *  Should be called by the instrumented class.
   */
  def start(timeStone: Long) {
    log.debug("Start " + timeStone)
    if (recursionDepth == 0) {
      firstTimeStone = timeStone
    }
    recursionDepth += 1
  }

  /** Sets values at the time exit th observed piece of code.
   *  Should be called by the instrumented class.
   *  When been back to the start of the recursion, calculates the running time.
   *  If the running time, `measured`, has been calculated before, simply ignore.
   */
  def end(timeStone: Long) {
    log.debug("End " + timeStone)
    recursionDepth -= 1
    if (recursionDepth == 0) {
      // Back to the start of the recursion
      if (measured == -1) {
        log.debug("Back to recursion start")
        measured = timeStone - firstTimeStone
      }
    }
  }

  /** Resets all the values for starting a new measurement.
   */
  def reset() {
    measured = -1
    recursionDepth = 0
    firstTimeStone = 0
  }

  protected val mode = Pinpointing

  def measure(benchmark: Benchmark): MeasurementResult = {
    val statistic = StatisticsFactory(log)
    log.info("[Benchmarking pinpointing regression detection]")
    benchmarkRunner run (
      benchmark,
      series => (statistic CoV series) < STEADY_THRESHOLD,
      {
        reset()
        benchmark.init()
        // TODO: should we sum `runs` of `measured`?
        benchmark.run()
        benchmark.reset()
        measured
      })
  }

}