/*
 * ArrayBufferSeries
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.util.FileUtil
import scala.tools.nsc.io.File
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode
import java.text.SimpleDateFormat
import java.util.Date
import scala.tools.sbs.util.Constant

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class ArrayBufferSeries(log: Log, config: Config) extends Series {

  /**
   *
   */
  private var data: ArrayBuffer[Long] = null

  /**
   *
   */
  private var _confidenceLevel: Int = 0
  def confidenceLevel = _confidenceLevel
  def confidenceLevel_=(confidenceLevel: Int) {
    _confidenceLevel = confidenceLevel
  }

  def this(log: Log, config: Config, series: ArrayBuffer[Long]) {
    this(log, config)
    data = series
  }

  def head = data.head

  def tail = new ArrayBufferSeries(log, config, data.tail)

  def last = data.last

  def length = data.length

  def clear() = data.clear()

  def +=(ele: Long) = {
    data += ele
    this
  }

  def foldLeft[B](z: B)(op: (B, Long) => B) = data.foldLeft[B](z)(op)

  def foldRight[B](z: B)(op: (Long, B) => B) = data.foldRight[B](z)(op)

  def forall(op: Long => Boolean) = data forall op

  def remove(n: Int) = data remove n

  /**
   * Calculates statistical metrics.
   *
   * @return
   * <ul>
   * <li>`true` if the ration between the confidence interval and the mean is less than the thredshold
   * <li>`false` otherwise
   * </ul>
   */
  def isReliable: Boolean = {

    if (length == 0) {
      log.debug("--Cleared result--")
      false
    } else if (length != config.multiplier) {
      log.debug("--Wrong in measurment length--")
      false
    } else {
      val statistic = new Statistic(log, config, 0)

      val mean = statistic mean this
      log.verbose("--Average--            " + (mean formatted "%.2f"))

      val (left, right) = statistic confidenceInterval this
      log.verbose("--Confident Interval-- [" + (left formatted "%.2f") + "; " +
        (right formatted "%.2f") + "]")

      var diff = (right - left)
      log.verbose("--Difference--         " + (diff formatted "%.2f") + " = " +
        ((diff / mean * 100) formatted "%.2f") + "%")

      while (statistic.isConfidenceLevelAcceptable && (diff / mean) >= Constant.CI_PRECISION_THREDSHOLD) {
        statistic.reduceConfidenceLevel()

        val (left, right) = statistic confidenceInterval this
        log.verbose("--Confident Interval-- [" + (left formatted "%.2f") + "; " +
          (right formatted "%.2f") + "]")

        diff = (right - left)
        log.verbose("--Difference--         " + (diff formatted "%.2f") + " = " +
          ((diff / mean * 100) formatted "%.2f") + "%")
      }

      if ((diff / mean) < Constant.CI_PRECISION_THREDSHOLD) {
        this.confidenceLevel = statistic.confidenceLevel.toInt
        true
      } else {
        false
      }
    }
  }

  /**
   *
   */
  def load(file: File) {
    for (line <- fromFile(file.path).getLines) {
      try {
        if (line startsWith "Date") {

        } else if (line startsWith "-") {

        } else if (line startsWith "Type") {

        } else if (line startsWith "Main") {

        } else if (line startsWith "Confidence") {
          this.confidenceLevel = (line split " ")(2).toInt
        } else {
          data += line.toLong
        }
      } catch {
        case e => {
          log.debug("[Read failed] " + file.path + e.toString)
          clear()
        }
      }
    }
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store(passed: Boolean): Option[File] = {
    if (series.length == 0) {
      log.info("Nothing to store")
      return None
    }
    val directory = if (passed) "" else (System getProperty "file.separator") + "FAILED"
    val data = new ArrayBuffer[String]
    data += "Date:             " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date)
    data += "Main Class:       " + benchmark.name
    data += "Type:             " + mode
    data += "Confidence level: " + confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      (config.persistorLocation / mode.toString).path + directory,
      benchmark.name + "." + metric.toString,
      series.foldLeft(data) { (data, l) => data + l.toString }
    )
  }

  /**
   *
   */
  override def toString(): String =
    foldLeft("Benchmarking result at " + confidenceLevel + "%: ") { (str, l) => str + "--" + l }

}
