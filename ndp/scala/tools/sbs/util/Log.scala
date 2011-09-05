/*
 * Log
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

import scala.tools.nsc.io.File

class Log(config: Config) {

  lazy val file = config.LOG_FILE
  lazy val level = config.LOG_LEVEL

  def apply(message: String) {
    Console println message
  }

  def info(message: String) {
    this("[Info]     " + message)
    if (config.SHOW_LOG) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    if (level == LogLevel.DEBUG) {
      this("[Debug]    " + message)
      if (config.SHOW_LOG) {
        UI("[Debug]    " + message)
      }
    }
  }

  def error(message: String) {
    this("[Error]    " + message)
    if (config.SHOW_LOG) {
      UI("[Error]    " + message)
    }
  }

  def verbose(message: String) {
    if (level == LogLevel.VERBOSE) {
      this("[Verbose]  " + message)
      if (config.SHOW_LOG) {
        UI("[Verbose]  " + message)
      }
    }
  }

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE = Value
}
