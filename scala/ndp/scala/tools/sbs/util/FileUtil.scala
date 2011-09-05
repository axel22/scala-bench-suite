/*
 * Config
 * 
 * Version
 * 
 * Created September 5th, 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

import java.io.{File => JFile}
import java.io.FileWriter
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Date

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File

import ndp.scala.tools.sbs.measurement.BenchmarkResult

object FileUtil {

  /**
   * Creates a new file for logging whose name in the format:
   * YYYYMMDD.hhmmss.BenchmarkClass.log
   */
  def createLog(benchmarkDir: Directory, classname: String, separator: String): File = {
    var filename: String = null
    var jfile: JFile = null
    var date: String = null

    //    log verbose this.toString

    while (filename == null) {

      date = new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date)
      filename = benchmarkDir.path + separator + date + classname + ".log"

     jfile = new JFile(filename)

      //      if (config.LOG_LEVEL == LogLevel.VERBOSE) {
      //        log verbose "Trying to store to " + file.getAbsolutePath
      //      }

      if (jfile exists) {

        //        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
        //          log verbose "File " + file.getName + " already exists"
        //        }

        sleep(1000)
        filename = null
      } else {
        try {
          val out = new FileWriter(jfile)
          out write "Logging for " + classname + " on " + new SimpleDateFormat("MM/dd/yyyy").format(new Date) + " at " + new SimpleDateFormat("HH:mm").format(new Date)
          out write (System getProperty "line.separator") + "-------------------------------"
          out close
        } catch {
          case e => {
            filename = null
            //            log debug e.toString
          }
        }
      }
    }
    new File(jfile)
    //    if (config.LOG_LEVEL == LogLevel.VERBOSE) {
    //      log verbose "Stored to " + new File(filename).getAbsolutePath
    //    }
  }
  
  /**
   * Creates result file
   */
  def storeResult(log: Log, config: Config, result: BenchmarkResult): String = {
    var filename: String = null

    log verbose this.toString

    while (filename == null) {

      filename = "output/Memory/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + "." + config.BENCHMARK_TYPE

      val file = new JFile(filename)

        log verbose "Trying to store to " + file.getAbsolutePath

      if (file exists) {

          log verbose "File " + file.getName + " already exists"

        sleep(1000)
        filename = null
      } else {
        try {
          val out = new FileWriter(filename)
          out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date) + "\n"
          out write "Main Class:	" + config.CLASSNAME + "\n"
          if (config.BENCHMARK_TYPE == BenchmarkType.STARTUP) {
            out write "Type:		Startup State Performance\n"
          } else if (config.BENCHMARK_TYPE == BenchmarkType.STEADY) {
            out write "Type:		Steady State Performance\n"
          } else {
            out write "Type:		Memory Consumption\n"
          }
          out write "-------------------------------\n"
          for (invidual <- result) {
            out write invidual.toString + "\n"
          }
          out close
        } catch {
          case e => {
            filename = null
            log debug e.toString
          }
        }
      }
    }
    filename
  }

}