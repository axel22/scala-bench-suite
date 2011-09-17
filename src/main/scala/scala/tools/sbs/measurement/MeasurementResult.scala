/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile
import scala.tools.nsc.io.File
import scala.tools.sbs.regression.Statistic
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.FileUtil
import scala.tools.sbs.util.Log

import BenchmarkType.BenchmarkType

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class MeasurementSeries(log: Log, config: Config, benchmark: Benchmark) {

  /**
   *
   */
  private var _series: ArrayBuffer[Long] = null
  def series = _series

  /**
   *
   */
  private var _confidenceLevel: Int = 0
  def confidenceLevel = _confidenceLevel
  def confidenceLevel_=(confidenceLevel: Int) {
    _confidenceLevel = confidenceLevel
  }

  /**
   *
   */
  private var _metric: BenchmarkType = null
  def metric = _metric
  def metric_=(metric: BenchmarkType) {
    _metric = metric
  }

  def this(log: Log, config: Config, benchmark: Benchmark, metric: BenchmarkType) {
    this(log, config, benchmark)
    this.metric = metric
  }

  def this(log: Log, config: Config, benchmark: Benchmark, series: ArrayBuffer[Long]) {
    this(log, config, benchmark)
    _series = series
  }

  def head = _series.head

  def tail = new MeasurementSeries(log, config, benchmark, _series.tail)

  def last = _series.last

  def length = _series.length

  def clear() = _series.clear()

  def +=(ele: Long) = _series += ele

  def foldLeft[B](z: B)(op: (B, Long) => B) = _series.foldLeft[B](z)(op)

  def foldRight[B](z: B)(op: (Long, B) => B) = _series.foldRight[B](z)(op)

  def forall(op: Long => Boolean) = _series forall op

  def remove(n: Int) = {
    _series remove n
    this
  }

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
          _series += line.toLong
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
    data += "Type:             " + metric
    data += "Confidence level: " + confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      (config.persistorLocation / metric.toString).path + directory,
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

class MeasurementResult(benchmark: Benchmark)

case class MeasurementSuccess(benchmark: Benchmark, series: MeasurementSeries)
  extends MeasurementResult(benchmark) {

}

case class MeasurementFailure(benchmark: Benchmark)
  extends MeasurementResult(benchmark) {

}

object BenchmarkType extends Enumeration {
  type BenchmarkType = Value
  val STARTUP, STEADY, MEMORY = Value
}
