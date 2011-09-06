/*
 * Persistor
 * 
 * Version
 * 
 * Created on September 5th, 2011
 * 
 * Created by ND P
 */
package ndp.scala.tools.sbs
package regression

import java.io.File
import java.io.FileFilter

import scala.collection.mutable.ArrayBuffer

import ndp.scala.tools.sbs.measurement.BenchmarkResult
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.LogLevel

class Persistor extends ArrayBuffer[BenchmarkResult] {

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(): Persistor = {
    var line: String = null
    var storedResult: BenchmarkResult = null
    val dir = new File(config.persistorLocation.path)

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

        log verbose "[Read file]	" + file.getAbsolutePath

        storedResult = new BenchmarkResult
        storedResult.load(file)

        log debug "[Read]	" + storedResult.toString

        this += storedResult
      } catch {
        case e => {
          log debug e.toString
          throw e
        }
      }
    }
    this
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store() {
    if (array.length == 0) {
      log("Nothing to store")
      return
    }
    for (result <- this) {
      result.store
    }
  }

}
