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
package strategy

import scala.tools.sbs.performance.regression.StatisticsFactory
import scala.tools.sbs.performance.MeasurementHarness
import scala.tools.sbs.performance.MeasurementResult

/** Measurer for pinpointing regression detection.
 *  Runs just like {@link scala.tools.sbs.performance.SteadyHarness}
 *  does, but only measures the first running time of a piece of code.
 */
object PinpointHarness extends MeasurementHarness[PinpointBenchmark] {

  protected val upperBound = manifest[PinpointBenchmark]

  /** Set only once at the first time measured.
   */
  private var measured = -1L

  /** Used to detect whether back to the first run of the recursion.
   */
  private var recursionDepth = 0

  /** Set only once as the first entry of the observed piece of code.
   */
  private var firstTimeStone = 0L

  /** Ordinal number of the running of the method call which is marked
   *  as the starting point to record time. Time recording is started
   *  at the time that method call expression is run for the ordinal
   *  number, equals to this value, time.
   *  The neccessary value of this field is set only once when entering
   *  the benchmark's pinpoint method.
   *  Current value is starting with 1.
   *  For example, consider the following pinpoint method:
   *  {{{
   *  def run() = for (_ <- 1 to 10) foo
   *  }}}
   *  if this variable is set to 5, time recording is started at the
   *  5th time `foo` is called.
   */
  private var startOrdinal = -1
  private var currentStartOrdinal = 0
  def setStartOrdinal(ordinal: Int) = if (startOrdinal == -1) startOrdinal = ordinal

  /** Ordinal number of the running of the method call which is marked
   *  as the ending point to record time. Time recording is stopped
   *  at the time that method call expression is run for the ordinal
   *  number, equals to this value, time.
   *  The neccessary value of this field is set only once when entering
   *  the benchmark's pinpoint method.
   *  Current value is starting with 1.
   *  For example, consider the following pinpoint method:
   *  {{{
   *  def run() = for (_ <- 1 to 10) foo
   *  }}}
   *  if this variable is set to 5, time recording is stopped at the
   *  5th time `foo` is called.
   */
  private var endOrdinal = -1
  private var currentEndOrdinal = 0
  def setEndOrdinal(ordinal: Int) = if (endOrdinal == -1) endOrdinal = ordinal

  /** Sets values at the time enter the observed piece of code.
   *  Should be called by the instrumented class.
   */
  def start(timeStone: Long) {
    log.debug("Start " + timeStone)
    if (recursionDepth == 0) {
      currentStartOrdinal += 1
      if (currentStartOrdinal == startOrdinal) firstTimeStone = timeStone
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
      log.debug("Back to recursion start")
      currentEndOrdinal += 1
      if (currentEndOrdinal == endOrdinal) {
        measured = timeStone - firstTimeStone
        log.debug("Runtime " + measured)
      }
    }
  }

  /** Resets all the values for starting a new measurement.
   */
  def reset() {
    measured = -1
    currentStartOrdinal = 0
    currentEndOrdinal = 0
  }

  protected val mode = Pinpointing

  def measure(benchmark: PinpointBenchmark): MeasurementResult = {
    val statistic = StatisticsFactory(config, log)
    log.info("[Benchmarking pinpointing regression detection]")
    seriesAchiever achieve (
      benchmark,
      series => (statistic CoV series) < config.precisionThreshold,
      () => {
        reset()
        benchmark.init()
        benchmark.run()
        benchmark.reset()
        if (measured == -1) {
          throw new Exception("Method " + benchmark.pinpointClass + "." + benchmark.pinpointMethod + " is never run")
        }
        else {
          measured
        }
      })
  }

}
