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

import java.io.{ File => JFile }
import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import ndp.scala.tools.sbs.measurement.BenchmarkResult
import ndp.scala.tools.sbs.measurement.SteadyHarness

class Persistor extends ArrayBuffer[BenchmarkResult] {

  private var _location: Directory = null
  def location = _location
  def location_=(location: Directory) {
    _location = location
  }

  def this(location: Directory) {
    this()
    this.location = location
  }

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(): Persistor = {
    var line: String = null
    var storedResult: BenchmarkResult = null
    val dir = new File(new JFile(location.path))

    log.debug("--Persistor directory--  " + dir.path)

    if (!dir.isDirectory || !dir.canRead) {
      log("[Cannot find previous results]")
    } else {
      val files = dir.walkFilter(path => path.isFile && path.canRead)
      for (file <- files) {
        try {
          log.verbose("[Read file]	" + file.path)

          storedResult = new BenchmarkResult
          storedResult.load(file.toFile)

          log.debug("[Read]	" + storedResult.toString)

          this += storedResult
        } catch {
          case e => {
            log.debug(e.toString)
          }
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

object Persistor extends Persistor {

  /**
   * Generates sample results.
   */
  def generate(num: Int) {
    var i = 0
    while (i < num) {
      SteadyHarness.run() match {
        case Left(ret) => {
          ret.store() match {
            case Some(_) => {
              log.debug("--Stored--")
              i += 1
              log.verbose("--Got " + i + " sample(s)--")
            }
            case _ => {
              log.debug("--Cannot store--")
            }
          }
        }
        case Right(s) => {
          log.debug("--At " + getClass().getName() + ": " + s + "--")
        }
      }
    }
  }

}
