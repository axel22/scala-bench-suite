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
package io

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.util.FileUtil

case class TextFileLog(logFile: File, override var config: Config) extends Log {

  def apply(message: String) {
    if (logFile != null) {
      FileUtil.write(logFile.path, message)
    }
  }

}

object TextFileLog {

  /** Creates a new file for logging whose name in the format:
   *  YYYYMMDD.hhmmss.log
   */
  def createLog(benchmarkDir: Directory): Option[File] = {
    var logInit = new ArrayBuffer[String]
    val date = new Date
    logInit += "Logging on " +
      new SimpleDateFormat("MM/dd/yyyy").format(date) + " at " + new SimpleDateFormat("HH:mm:ss").format(date)
    logInit += "-------------------------------"
    FileUtil.createAndStore(benchmarkDir.path, "log", logInit)
  }

  /** Creates a new file for logging whose name in the format:
   *  YYYYMMDD.hhmmss.Benchmark.log
   */
  def createLog(benchmarkName: String, mode: BenchmarkMode, benchmarkDir: Directory): Option[File] = {
    var logInit = new ArrayBuffer[String]
    val date = new Date
    logInit += "Logging for " + benchmarkName + " on " +
      new SimpleDateFormat("MM/dd/yyyy").format(date) + " at " + new SimpleDateFormat("HH:mm:ss").format(date)
    logInit += "-------------------------------"
    FileUtil.createAndStore((benchmarkDir / mode.location).path, benchmarkName + ".log", logInit)
  }

}
