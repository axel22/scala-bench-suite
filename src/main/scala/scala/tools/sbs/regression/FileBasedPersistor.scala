/*
 * SimpleFilePersistor
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Constant.SLASH
import scala.tools.sbs.util.FileUtil

class FileBasedPersistor(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode) extends Persistor {

  val location: Directory = FileUtil.mkDir(config.history / mode.location / benchmark.name) match {
    case Left(dir) => dir
    case Right(s) => {
      log.error(s)
      FileUtil.mkDir(config.history / mode.location) match {
        case Left(dir) => dir
        case Right(s) => {
          log.error(s)
          config.benchmarkDirectory
        }
      }
    }
  }

  /** Generates sample results.
   */
  def generate(num: Int): History = {
    var i = 0
    val measurer = MeasurerFactory(config, mode)
    var justCreated = HistoryFactory(log, config, benchmark, mode)
    while (i < num) {
      measurer measure benchmark match {
        case success: MeasurementSuccess => {

          storeToFile(success, BenchmarkSuccess(benchmark, mode, success.series.confidenceLevel, success)) match {
            case Some(_) => {
              log.debug("--Stored--")
              i += 1
              log.verbose("--Got " + i + " sample(s)--")
            }
            case _ => log.debug("--Cannot store--")
          }
          justCreated add success.series
        }
        case failure: MeasurementFailure =>
          log.debug("--Generation error at " + i + ": " + failure.reason + "--")
      }
    }
    justCreated
  }

  def load(): History = loadFromFile

  def loadFromFile(): History = {

    var line: String = null
    var series: Series = null
    var justLoaded = HistoryFactory(log, config, benchmark, mode)

    log.debug("--Persistor directory--  " + location.path)

    if (!location.isDirectory || !location.canRead) {
      log.info("--Cannot find previous results--")
    } else {
      location walkFilter (path => path.isFile && path.canRead && (path.toFile hasExtension mode.toString)) foreach (
        file => try {
          log.verbose("--Read file--	" + file.path)

          series = loadSeries(file.toFile)

          log.debug("----Read----	" + series.toString)

          if (series != null && series.length > 0) {
            justLoaded add series
          }
        } catch {
          case e => {
            log.debug(e.toString)
          }
        })
    }
    justLoaded
  }

  def loadSeries(file: File): Series = {
    var dataSeries = ArrayBuffer[Long]()
    var confidenceLevel = 0
    for (line <- Source.fromFile(file.path).getLines) {
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
    new Series(log, dataSeries, confidenceLevel)
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
      case BenchmarkSuccess(_, _, _, _) => ""
      case NoPreviousFailure(_, _, _) => ""
      case _ => FileUtil.mkDir(location / "FAILED") match {
        case Left(_) => SLASH + "FAILED"
        case _ => ""
      }
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
      measurement.series.foldLeft(data) { (data, l) => data + l.toString })
  }

}
