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
package util

import scala.tools.nsc.io.File
import scala.tools.sbs.util.LogLevel.LogLevel
import scala.tools.nsc.io.Directory

class LogFactory(benchmarkName: String, logLevel: LogLevel, logShow: Boolean) {

  def create(benchmarkDir: Directory): Log = {
    TextFileLog createLog (benchmarkDir, benchmarkName) match {
      case Some(logFile) => new TextFileLog(logFile, logLevel, logShow)
      case None => UI
    }
  }

}
