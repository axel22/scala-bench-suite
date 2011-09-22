/*
 * MeasurementSignal
 * 
 * Version
 * 
 * Created on September 21st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

/**
 * Constants for interfacing between measuring processes.
 */
object MeasurementSignal {

  val LOG_SIGNAL = ""

  val RESULT_START = "[----]"

  val MEASUREMENT_SUCCESS = "[Measurement OK]"
  val MEASUREMENT_FAILURE_UNWARMABLE = "[Benchmark could not reach steady state]"
  val MEASUREMENT_FAILURE_UNRELIABLE = "[Measured metric is unreliable]"
  val MEASUREMENT_FAILURE_PROCESS_FAIL = "[Benchmark process could not run]"
  val MEASUREMENT_FAILURE_EXCEPTION = "[Benchmark raised exception]"

}
