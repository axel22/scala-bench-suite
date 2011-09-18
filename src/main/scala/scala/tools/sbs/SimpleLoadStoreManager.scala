package scala.tools.sbs

import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.regression.Persistor
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.util.FileUtil
import java.text.SimpleDateFormat
import scala.tools.sbs.measurement.MeasurementResult
import java.util.Date
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.io.Source.fromFile
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.SeriesFactory
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.regression.PersistorFactory

class SimpleLoadStoreManager(
  log: Log, config: Config, benchmark: Benchmark, location: Directory, mode: BenchmarkMode) extends LoadStoreManager {

  /**
   *
   */
  def loadPersistor(): Persistor = {
    
    var line: String = null
    var series: Series = null
    var persistor = new PersistorFactory(log, config, benchmark) create location

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
            persistor add series
          }
        } catch {
          case e => {
            log.debug(e.toString)
          }
        }
      )
    }
    persistor
  }
  
  def loadSeries(file: File): Series = {
    var dataSeries = ArrayBuffer[Long]()
    var confidenceLevel = 0
    for (line <- fromFile(file.path).getLines) {
      try {
        if (line startsWith "Date") {

        } else if (line startsWith "-") {

        } else if (line startsWith "Type") {

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
    new SeriesFactory(log, config).create(dataSeries, confidenceLevel)
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def storeMeasurementResult(result: MeasurementResult): Option[File] = {
    if (result.series.length == 0) {
      log.info("Nothing to store")
      return None
    }
    val directory = result match {
      case MeasurementSuccess(_) => ""
      case _ => (System getProperty "file.separator") + "FAILED"
    }
    val data = new ArrayBuffer[String]
    data += "Date:             " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date)
    data += "Main Class:       " + benchmark.name
    data += "Mode:             " + mode
    data += "Confidence level: " + result.series.confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      (location / mode.toString).path + directory,
      benchmark.name + "." + mode.toString,
      result.series.foldLeft(data) { (data, l) => data + l.toString }
    )
  }

}
