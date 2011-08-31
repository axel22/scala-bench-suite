package ndp.scala.benchmarksuite
package regression

import java.io.File
import java.io.FileFilter
import java.io.FileWriter
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile

import ndp.scala.benchmarksuite.measurement.BenchmarkResult
import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log
import ndp.scala.benchmarksuite.utility.LogLevel

class Persistor(log: Log, config: Config) extends ArrayBuffer[BenchmarkResult] {

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(): Persistor = {
    var line: String = null
    var storedResult: BenchmarkResult = null
    val dir = new File(config.PERSISTOR_LOC.path)

    if (config.LOG_LEVEL == LogLevel.DEBUG) {
      log debug ("Persistor directory: " + dir.getAbsolutePath)
    }

    if (!dir.isDirectory || !dir.canRead) {
      throw new Exception("Cannot find previous result")
    }

    val files = dir.listFiles(new FileFilter {
      override def accept(file: File): Boolean = {
        file.isFile && file.canRead
      }
    })

    for (file <- files) {
      try {

        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
          log verbose "[Read file]	" + file.getAbsolutePath()
        }

        storedResult = new BenchmarkResult

        for (line <- fromFile(file getAbsolutePath) getLines) {
          try {
            if (line startsWith "Date") {

            } else if (line startsWith "-") {

            } else if (line startsWith "Type") {

            } else if (line startsWith "Main") {

            } else {
              storedResult += line.toLong
            }
          } catch {
            case e => {
              log(e.toString)
              throw new Exception("In file " + file.getName + ": " + line)
            }
          }
        }

        if (config.LOG_LEVEL == LogLevel.DEBUG) {
          log debug "[Read]	" + storedResult.toString
        }

        this += storedResult
      } catch {
        case e => {
          if (config.LOG_LEVEL == LogLevel.DEBUG) {
            log debug e.toString
          } else {
            throw e
          }
        }
      }
    }
    this
  }

  /**
   * Stores a result value series in the field <code>Series</code> in to text file whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store() {
    if (array.length == 0) {
      log("Nothing to store")
      return
    }

    var filename: String = null

    for (result <- this) {

      log verbose result.toString

      while (filename == null) {
        if (config.BENCHMARK_TYPE == BenchmarkType.STARTUP) {
          filename = "output/Startup/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".StartupState"
        } else if (config.BENCHMARK_TYPE == BenchmarkType.STEADY) {
          filename = "output/Steady/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".SteadyState"
        } else {
          filename = "output/Memory/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".MemoryConsumption"
        }

        val file = new File(filename)

        if (config.LOG_LEVEL == LogLevel.VERBOSE) {
          log verbose "Trying to store to " + file.getAbsolutePath
        }

        if (file exists) {

          if (config.LOG_LEVEL == LogLevel.VERBOSE) {
            log verbose "File " + file.getName + " already exists"
          }

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
      if (config.LOG_LEVEL == LogLevel.VERBOSE) {
        log verbose "Stored to " + new File(filename).getAbsolutePath
      }
    }
  }

}
