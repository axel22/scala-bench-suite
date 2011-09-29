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

trait Log {

  protected var config: Config

  def apply(message: String)

  def info(message: String) {
    this("[Info]     " + message)
    if (config.isShowLog) {
      UI("[Info]     " + message)
    }
  }

  def debug(message: String) {
    if (config.isDebug) {
      this("[Debug]    " + message)
      if (config.isShowLog) {
        UI("[Debug]    " + message)
      }
    }
  }

  def error(message: String) {
    this("[Error]    " + message)
    if (config.isShowLog) {
      UI("[Error]    " + message)
    }
  }

  def verbose(message: String) {
    if (config.isVerbose) {
      this("[Verbose]  " + message)
      if (config.isShowLog) {
        UI("[Verbose]  " + message)
      }
    }
  }

}

object LogFactory {

  def apply(config: Config): Log = {
    TextFileLog.createLog(config.benchmarkDirectory) match {
      case Some(logFile) => new TextFileLog(logFile, config)
      case None => UI
    }
  }

  def apply(benchmarkName: String, mode: BenchmarkMode, config: Config): Log = {
    TextFileLog.createLog(benchmarkName, mode: BenchmarkMode, config.benchmarkDirectory) match {
      case Some(logFile) => new TextFileLog(logFile, config)
      case None => UI
    }
  }

}
