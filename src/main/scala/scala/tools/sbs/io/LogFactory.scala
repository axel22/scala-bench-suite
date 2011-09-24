/*
 * LogFactory
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

import scala.tools.nsc.io.Directory
import scala.tools.sbs.io.LogLevel.LogLevel

class LogFactory {

  def create(benchmarkName: String, benchmarkDir: Directory, logLevel: LogLevel, logShow: Boolean): Log = {
    TextFileLog.createLog(benchmarkDir, benchmarkName) match {
      case Some(logFile) => new TextFileLog(logFile, logLevel, logShow)
      case None => UI
    }
  }

  def create(logLevel: LogLevel): Log = {
    new SubProcessLog(logLevel)
  }

}
