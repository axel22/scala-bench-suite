/*
 * ArrayBufferPersistor
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.nsc.io.File
import scala.io.Source.fromFile
import scala.tools.sbs.util.FileUtil
import java.text.SimpleDateFormat
import java.util.Date

class ArrayBufferPersistor(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode)
  extends Persistor with FilePersistor {

  private var data: ArrayBuffer[Series] = null

  def this(log: Log,
           config: Config,
           benchmark: Benchmark,
           mode: BenchmarkMode,
           data: ArrayBuffer[Series]) {
    this(log, config, benchmark, mode)
    this.data = data
  }

  def location = (benchmark.directory / "result" / mode.toString).toDirectory

  def mode(): BenchmarkMode = mode

  /**
   * Add a `Series` to `data`.
   */
  def add(ele: Series) = {
    data += ele
    this
  }

  def concat(that: Persistor): Persistor = {
    that foreach (this add _)
    this
  }

  def apply(i: Int) = data(i)

  def foldLeft[B](z: B)(op: (B, Series) => B): B = data.foldLeft[B](z)(op)

  def head: Series = data.head

  def last = data.last

  def tail = new ArrayBufferPersistor(log, config, benchmark, mode, data.tail)

  def length = data.length

  def foreach(f: Series => Unit): Unit = data foreach f

  def forall(op: Series => Boolean) = data forall op

  /**
   * Generates sample results.
   */
  def generate(num: Int): Persistor = {
    var i = 0
    val measurer = new MeasurerFactory(log, config) create mode
    while (i < num) {
      measurer run benchmark match {
        case success: MeasurementSuccess => {

          storeToFile(success, BenchmarkSuccess(success.series.confidenceLevel, success)) match {
            case Some(_) => {
              log.debug("--Stored--")
              i += 1
              log.verbose("--Got " + i + " sample(s)--")
            }
            case _ => {
              log.debug("--Cannot store--")
            }
          }
          this add success.series
        }
        case failure: MeasurementFailure => {
          log.debug("--Generation error at " + i + ": " + failure.reason + "--")
        }
      }
    }
    this
  }

  def load(): Persistor = {
    loadFromFile
    this
  }

  def loadFromFile(): FilePersistor = {

    var line: String = null
    var series: Series = null

    log.debug("--Persistor directory--  " + location.path)

    if (!location.isDirectory || !location.canRead) {
      log.info("--Cannot find previous results--")
    } else {
      location walkFilter (path => path.isFile && path.canRead) foreach (
        file => try {
          log.verbose("--Read file--	" + file.path)

          series = loadSeries(file.toFile)

          log.debug("----Read----	" + series.toString)

          if (series != null && series.length > 0) {
            this add series
          }
        } catch {
          case e => {
            log.debug(e.toString)
          }
        }
      )
    }
    this
  }

  def loadSeries(file: File): Series = {
    var dataSeries = ArrayBuffer[Long]()
    var confidenceLevel = 0
    for (line <- fromFile(file.path).getLines) {
      try {
        if (line startsWith "Date") {

        } else if (line startsWith "-") {

        } else if (line startsWith "Mode") {

        } else if (line startsWith "Main") {

        } else if (line startsWith "Confidence") {
          confidenceLevel = (line split " ")(2).toInt
        } else {
          dataSeries += line.toLong
        }
      } catch {
        case e => {
          log.debug("[Read failed] " + file.path + e.toString)
          dataSeries.clear()
          return null
        }
      }
    }
    new Series(log, config, dataSeries, confidenceLevel)
  }

  def store(measurement: MeasurementSuccess, result: BenchmarkResult): Boolean = {
    storeToFile(measurement, result) match {
      case Some(file) => {
        log.info("Result stored OK into " + file.path)
        true
      }
      case _ => {
        log.info("Cannot store measurement result")
        false
      }
    }
  }

  def storeToFile(measurement: MeasurementSuccess, result: BenchmarkResult): Option[File] = {
    if (measurement.series.length == 0) {
      log.info("Nothing to store")
      return None
    }
    val directory = result match {
      case BenchmarkSuccess(_, result) => ""
      case _ => (System getProperty "file.separator") + "FAILED"
    }
    val data = new ArrayBuffer[String]
    data += "Date:             " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date)
    data += "Main Class:       " + benchmark.name
    data += "Mode:             " + mode
    data += "Confidence level: " + measurement.series.confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      location.path + directory,
      benchmark.name + "." + mode.toString,
      measurement.series.foldLeft(data) { (data, l) => data + l.toString }
    )
  }

}
