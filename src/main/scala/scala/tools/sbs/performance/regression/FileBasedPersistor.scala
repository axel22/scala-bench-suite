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
package performance
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Path.string2path
import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.util.Constant.SLASH
import scala.tools.sbs.util.FileUtil
import scala.xml.XML

/** An implement of {@link Persistor} based on simple text files.
 */
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

  def load(): History = loadFromFile

  def loadFromFile(): History = {

    var line: String = null
    var series: Series = null
    var justLoaded = HistoryFactory(log, config, benchmark, mode)

    log.debug("--Persistor directory--  " + location.path)

    if (!location.isDirectory || !location.canRead) {
      log.info("--Cannot find previous results--")
    }
    else {
      location walkFilter (path => path.isFile && path.canRead && (path.toFile hasExtension "xml")) foreach (
        file => try {
          log.verbose("--Read file--	" + file.path)

          series = loadSeries(file.toFile)

          log.debug("----Read----	" + series.toString)

          if (series != null && series.length > 0) {
            justLoaded add series
          }
        }
        catch {
          case e => {
            UI.error(e.toString)
            log.debug(e.toString)
          }
        })
    }
    justLoaded
  }

  def loadSeries(file: File): Series = {
    val xml = XML.loadFile(file.path)
    try {
      val confidenceLevel = (xml \\ "confidenceLevel").text.toInt
      var dataSeries = ArrayBuffer[Long]()
      (xml \\ "value") foreach (dataSeries += _.text.toLong)
      new Series(config, log, dataSeries, confidenceLevel)
    }
    catch {
      case e => {
        UI.error("[Read failed] " + file.path + e.toString)
        log.debug("[Read failed] " + file.path + e.toString)
        log.debug(e.getStackTraceString)
        null
      }
    }
  }

  /** Generates sample results.
   */
  def generate(num: Int): History = {
    var i = 0
    val runner = RunnerFactory(config, log, mode)
    var justCreated = HistoryFactory(log, config, benchmark, mode)
    while (i < num) {
      runner run benchmark match {
        case success: MeasurementSuccess => {

          if (store(success, true)) {
            log.debug("--Stored--")
            i += 1
            log.verbose("--Got " + i + " sample(s)--")
          }
          else {
            log.debug("--Cannot store--")
          }
          justCreated add success.series
        }
        case failure: MeasurementFailure =>
          log.debug("--Generation error at " + i + ": " + failure.reason + "--")
        case _ => throw new Error("WTF is just created?")
      }
    }
    justCreated
  }

  def store(runSuccess: RunSuccess, benchmarkSuccess: Boolean): Boolean = {
    val directory = if (benchmarkSuccess) "" else {
      FileUtil.mkDir(location / "FAILED") match {
        case Left(_) => SLASH + "FAILED"
        case _       => ""
      }
    }
    FileUtil.createFile(location.path + directory, benchmark.name + "." + mode.toString + ".xml") match {
      case Some(xmlFile) => {
        log.info("Result stored OK into " + xmlFile.path)
        XML.save(xmlFile.path, runSuccess.toXML, "UTF-8", true, null)
        true
      }
      case None => {
        log.info("Cannot store run result")
        false
      }
    }
  }

}
