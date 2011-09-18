/*
 * TextFileLog
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package util

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.util.LogLevel.LogLevel

class TextFileLog(logFile: File, logLevel: LogLevel, logShow: Boolean) extends Log {

  def logShow() = logShow
  
  def logLevel() = logLevel
  
  def apply(message: String) {
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
