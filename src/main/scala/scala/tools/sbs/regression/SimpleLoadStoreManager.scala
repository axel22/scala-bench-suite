/*
 * SimpleLoadStoreManager
 * 
 * Version
 * 
 * Created on Septemer 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.measurement.SeriesFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.FileUtil
import scala.tools.sbs.util.Log

class SimpleLoadStoreManager(
  log: Log, config: Config, benchmark: Benchmark, location: Directory, mode: BenchmarkMode) extends LoadStoreManager {

  /**
   *
   */
  def loadFromFile(): FilePersistor = {null}
  
  def loadPersistor(): Persistor = null

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
    new SeriesFactory(log, config).create(dataSeries, confidenceLevel)
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def storeMeasurementResult(result: MeasurementSuccess, regression: BenchmarkResult): Option[File] = {
    if (result.series.length == 0) {
      log.info("Nothing to store")
      return None
    }
    val directory = regression match {
      case BenchmarkSuccess(_, result) => ""
      case _ => (System getProperty "file.separator") + "FAILED"
    }
    val data = new ArrayBuffer[String]
    data += "Date:             " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date)
    data += "Main Class:       " + benchmark.name
    data += "Mode:             " + mode
    data += "Confidence level: " + result.series.confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      location.path + directory,
      benchmark.name + "." + mode.toString,
      result.series.foldLeft(data) { (data, l) => data + l.toString }
    )
  }

}
