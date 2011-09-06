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

import java.io.{ File => JFile }

import scala.tools.nsc.io.File

import LogLevel.LogLevel

class Log(logFile: File, logLevel: LogLevel, logShow: Boolean) {

  def this(args: Array[String]) {
    this(
      new File(new JFile(args(Constant.INDEX_LOG_FILE))),
      if (args(Constant.INDEX_LOG_LEVEL) equals "debug") {
        LogLevel.DEBUG
      } else if (args(Constant.INDEX_LOG_LEVEL) equals "verbose") {
        LogLevel.VERBOSE
      } else {
        LogLevel.INFO
      },
      args(Constant.INDEX_SHOW_LOG).toBoolean)
  }

  def apply(message: String) {
    Console println message
  }

  def info(message: String) {
    this("[Info]     " + message)
    if (logShow) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    if (logLevel == LogLevel.DEBUG) {
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
    if (logLevel == LogLevel.VERBOSE) {
      this("[Verbose]  " + message)
      if (logShow) {
        UI("[Verbose]  " + message)
      }
    }
  }

  def toArgument(): String = {
    var arr = new Array[String](Constant.MAX_ARGUMENT_LOG)
    arr(Constant.INDEX_LOG_FILE) = logFile.path
    arr(Constant.INDEX_LOG_LEVEL) = logLevel.toString
    arr(Constant.INDEX_SHOW_LOG) = logShow.toString
    arr mkString " "
  }

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE = Value
}
