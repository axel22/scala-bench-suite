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
import scala.tools.sbs.measurement.BenchmarkType.BenchmarkType
import scala.tools.sbs.measurement.Benchmark
import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.measurement.MeasurementSeries
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.measurement.OverallHarness
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurementFailure

class Persistor(log: Log, config: Config, benchmark: Benchmark, location: Directory) {

  private var _container: ArrayBuffer[MeasurementSeries] = null
  def container = _container
  private def container_=(container: ArrayBuffer[MeasurementSeries]) {
    _container = container
  }

  def location(): Directory = location

  def this(
    log: Log, config: Config, benchmark: Benchmark, location: Directory, container: ArrayBuffer[MeasurementSeries]) {
    this(log, config, benchmark, location)
    this.container = container
  }
  /**
   * Add a `MeasurementSeries` to `container`.
   */
  def add(result: MeasurementSeries) = {
    container += result
    this
  }

  def apply(i: Int) = container(i)

  def foldLeft[B](z: B)(op: (B, MeasurementSeries) => B): B = container.foldLeft[B](z)(op)

  def head = container.head

  def last = container.last

  def tail = new Persistor(log, config, benchmark, location, container.tail)

  def length = container.length

  def foreach(f: MeasurementSeries => Unit): Unit = container foreach f

  def forall(op: MeasurementSeries => Boolean) = container forall op

  /**
   * Loads previous benchmark result from local directory.
   */
  def load(metric: BenchmarkType): Persistor = {
    var line: String = null
    var storedResult: MeasurementSeries = null

    log.debug("--Persistor directory--  " + location.path)

    if (!location.isDirectory || !location.canRead) {
      log.info("--Cannot find previous results--")
    } else {
      location walkFilter (path => path.isFile && path.canRead) foreach (
        file => try {
          log.verbose("--Read file--	" + file.path)

          storedResult = new MeasurementSeries(log, config, benchmark)
          storedResult.load(file.toFile)

          log.debug("----Read----	" + storedResult.toString)

          container += storedResult
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
  def store() = container foreach (_.store(true))

  /**
   * Generates sample results.
   */
  def generate(metric: BenchmarkType, num: Int) {
    var i = 0
    val harness = new OverallHarness(log, config)
    while (i < num) {
      harness run benchmark match {
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
