package ndp.scala.benchmarksuite.regression

import java.io.File
import java.io.FileFilter
import java.io.FileWriter
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile

import ndp.scala.benchmarksuite.utility.BenchmarkType
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log

class Persistor(log: Log, config: Config) extends ArrayBuffer[BenchmarkResult] {

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(): Persistor = {
    var line: String = null
    var storedResult: BenchmarkResult = null
    val dir = new File(config.PERSISTOR_LOC)

    log debug ("Persistor directory: " + dir.getAbsolutePath)

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
        
        log verbose "[Read file]	" + file.getAbsolutePath()
        
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
        
        log debug "[Read]	" + storedResult.toString
        
        this += storedResult
      } catch {
        case e => log(e.toString)
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
        if (config.BENCHMARK_TYPE == BenchmarkType.Startup) {
          filename = "output/Startup/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".StartupState"
        } else if (config.BENCHMARK_TYPE == BenchmarkType.Steady) {
          filename = "output/Steady/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".SteadyState"
        } else {
          filename = "output/Memory/" + new SimpleDateFormat("yyyyMMdd.HHmmss.").format(new Date) + config.CLASSNAME + ".MemoryConsumption"
        }

        val file = new File(filename)
        
        log verbose file.getAbsolutePath()
        
        if (file exists) {
          log verbose file.getAbsolutePath()
          sleep(1000)
          filename = null
        } else {
          
          log verbose file.getAbsolutePath()
          
          try {
            val out = new FileWriter(filename)
            out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date) + "\n"
            out write "Main Class:	" + config.CLASSNAME + "\n"
            if (config.BENCHMARK_TYPE == BenchmarkType.Startup) {
              out write "Type:		Startup State Performance\n"
            } else if (config.BENCHMARK_TYPE == BenchmarkType.Steady) {
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
              log(e.toString())
            }
          }
        }
      }
      log verbose "Stored to " + new File(filename).getAbsolutePath
    }
  }

}
