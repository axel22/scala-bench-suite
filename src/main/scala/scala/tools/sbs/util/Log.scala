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

import java.io.{ File => JFile }
import scala.tools.nsc.io.File
import LogLevel.LogLevel
import java.io.FileWriter
import scala.tools.nsc.io.Directory
import java.text.SimpleDateFormat
import scala.collection.mutable.ArrayBuffer
import java.util.Date

trait Log {

  var logShow = false
  var logLevel: LogLevel = _

  protected def apply(message: String)

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

class TextFileLog(logFile: File, logLevel: LogLevel, logShow: Boolean) extends Log {

  def write(message: String) {
    if (logFile != null) {
      FileUtil.write(logFile.path, message)
    }
  }

}

object TextFileLog {
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
