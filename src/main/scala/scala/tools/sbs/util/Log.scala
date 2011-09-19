/*
 * Log
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

import LogLevel.LogLevel

trait Log {

  def logShow: Boolean
  
  def logLevel: LogLevel

  def apply(message: String)

  def info(message: String) {
    this("[Info]     " + message)
    if (logShow) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    if (logLevel == LogLevel.DEBUG || logLevel == LogLevel.ALL) {
      this("[Debug]    " + message)
      if (logShow) {
        UI("[Debug]    " + message)
      }
    }
  }

  def error(message: String) {
    this("[Error]    " + message)
    if (logShow) {
      UI("[Error]    " + message)
    }
  }

  def verbose(message: String) {
    if (logLevel == LogLevel.VERBOSE || logLevel == LogLevel.ALL) {
      this("[Verbose]  " + message)
      if (logShow) {
        UI("[Verbose]  " + message)
      }
    }
  }

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE, ALL = Value
}
