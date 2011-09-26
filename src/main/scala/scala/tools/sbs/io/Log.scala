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
package io

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

trait Log {

  protected val config: Config

  def logFile: File

  def apply(message: String)

  def info(message: String) {
    this("[Info]     " + message)
    if (config.showLog) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    if (config.logLevel == LogLevel.DEBUG || config.logLevel == LogLevel.ALL) {
      this("[Debug]    " + message)
      if (config.showLog) {
        UI("[Debug]    " + message)
      }
    }
  }

  def error(message: String) {
    this("[Error]    " + message)
    if (config.showLog) {
      UI("[Error]    " + message)
    }
  }

  def verbose(message: String) {
    if (config.logLevel == LogLevel.VERBOSE || config.logLevel == LogLevel.ALL) {
      this("[Verbose]  " + message)
      if (config.showLog) {
        UI("[Verbose]  " + message)
      }
    }
  }

  def toXML: scala.xml.Elem

}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val INFO, DEBUG, VERBOSE, ALL = Value
}

object LogFactory {

  def apply(benchmarkDir: Directory, config: Config): Log = {
    TextFileLog.createLog(benchmarkDir) match {
      case Some(logFile) => new TextFileLog(logFile, config)
      case None => UI
    }
  }

  def apply(benchmarkName: String, benchmarkDir: Directory, config: Config): Log = {
    TextFileLog.createLog(benchmarkName, benchmarkDir) match {
      case Some(logFile) => new TextFileLog(logFile, config)
      case None => UI
    }
  }

}
