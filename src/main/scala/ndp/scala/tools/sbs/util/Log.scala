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
import java.io.FileWriter
import scala.tools.nsc.io.Directory
import java.text.SimpleDateFormat
import scala.collection.mutable.ArrayBuffer
import java.util.Date

class Log {

  var logFile: File = null
  var logLevel: LogLevel = LogLevel.INFO
  var logShow = false

  def this(logFile: File, logLevel: LogLevel, logShow: Boolean) {
    this
    this.logFile = logFile
    this.logLevel = logLevel
    this.logShow = logShow
  }

  def this(args: Array[String]) {
    this(
      new File(new JFile(args(Constant.INDEX_LOG_FILE))),
      if (args(Constant.INDEX_LOG_LEVEL) equals LogLevel.DEBUG.toString()) {
        LogLevel.DEBUG
      } else if (args(Constant.INDEX_LOG_LEVEL) equals LogLevel.VERBOSE.toString()) {
        LogLevel.VERBOSE
      } else if (args(Constant.INDEX_LOG_LEVEL) equals LogLevel.ALL.toString()) {
        LogLevel.ALL
      } else {
        LogLevel.INFO
      },
      args(Constant.INDEX_SHOW_LOG).toBoolean)
  }

  private def apply(message: String) {
    if (logFile != null) {
      FileUtil.write(logFile.path, message)
    }
  }

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

  def toArgument(): Array[String] = {
    var arr = new Array[String](Constant.MAX_ARGUMENT_LOG)
    arr(Constant.INDEX_LOG_FILE) = logFile.path
    arr(Constant.INDEX_LOG_LEVEL) = logLevel.toString
    arr(Constant.INDEX_SHOW_LOG) = logShow.toString
    arr
  }

}

object Log extends Log {
  /**
   * Creates a new file for logging whose name in the format:
   * YYYYMMDD.hhmmss.BenchmarkClass.log
   */
  def createLog(benchmarkDir: Directory, classname: String): Option[File] = {
    var logInit = new ArrayBuffer[String]
    val date = new Date
    logInit += "Logging for " + classname + " on " +
      new SimpleDateFormat("MM/dd/yyyy").format(date) + " at " + new SimpleDateFormat("HH:mm:ss").format(date)
    logInit += "-------------------------------"
    FileUtil.createAndStore(benchmarkDir.path, classname + ".log", logInit)
  }

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE, ALL = Value
}
