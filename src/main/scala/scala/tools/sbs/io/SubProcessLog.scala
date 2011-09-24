/*
 * SubProcessLog
 * 
 * Version
 * 
 * Created on September 22nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

import scala.tools.sbs.util.LogLevel.LogLevel
import scala.tools.sbs.measurement.MeasurementSignal

/**
 * Logging from a separated process.
 */
class SubProcessLog(logLevel: LogLevel) extends Log {
  
  def logShow = false
  
  def logLevel() = logLevel
  
  def apply(message: String) {
    Console println MeasurementSignal.LOG_SIGNAL + message
  }

}
