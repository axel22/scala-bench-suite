/*
 * Persistor
 * 
 * Version
 * 
 * Created on September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.measurement.MeasurerFactory

class Persistor(log: Log, config: Config, benchmark: Benchmark, location: Directory) {

  private var data: ArrayBuffer[MeasurementResult] = null
  
  def location(): Directory = location

  def this(
    log: Log, config: Config, benchmark: Benchmark, location: Directory, data: ArrayBuffer[MeasurementResult]) {
    this(log, config, benchmark, location)
    this.data = data
  }
  /**
   * Add a `MeasurementResult` to `data`.
   */
  def add(ele: MeasurementResult) = {
    data += ele
    this
  }

  def apply(i: Int) = data(i)

  def foldLeft[B](z: B)(op: (B, MeasurementResult) => B): B = data.foldLeft[B](z)(op)

  def head = data.head

  def last = data.last

  def tail = new Persistor(log, config, benchmark, location, data.tail)

  def length = data.length

  def foreach(f: MeasurementResult => Unit): Unit = data foreach f

  def forall(op: MeasurementResult => Boolean) = data forall op

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(mode: BenchmarkMode): Persistor = {
    var line: String = null
    var storedResult: MeasurementResult = null

    log.debug("--Persistor directory--  " + location.path)

    if (!location.isDirectory || !location.canRead) {
      log.info("--Cannot find previous results--")
    } else {
      location walkFilter (path => path.isFile && path.canRead) foreach (
        file => try {
          log.verbose("--Read file--	" + file.path)

          storedResult = new MeasurementResult(log, config, benchmark)
          storedResult.load(file.toFile)

          log.debug("----Read----	" + storedResult.toString)

          data += storedResult
        } catch {
          case e => {
            log.debug(e.toString)
          }
        }
      )
    }
    this
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store() = data foreach (_.store(true))

  /**
   * Generates sample results.
   */
  def generate(mode: BenchmarkMode, num: Int) {
    var i = 0
    val measurer = new MeasurerFactory(log, config) create mode
    while (i < num) {
      measurer run benchmark match {
        case success: MeasurementSuccess => {
          success.series store true match {
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
        case failure: MeasurementFailure => {
          log.debug("--At " + getClass().getName() + ": " + s + "--")
        }
      }
    }
  }

}
