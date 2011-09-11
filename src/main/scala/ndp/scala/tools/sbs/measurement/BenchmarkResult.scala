/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package measurement

import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile
import scala.tools.nsc.io.File

import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.util.FileUtil

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class BenchmarkResult extends ArrayBuffer[Long] {

  /**
   *
   */
  private var _confidenceLevel: Int = 0
  def confidenceLevel = _confidenceLevel
  def confidenceLevel_=(confidenceLevel: Int) {
    _confidenceLevel = confidenceLevel
  }

  def this(confidenceLevel: Int) {
    this()
    this.confidenceLevel = confidenceLevel
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
      Statistic.resetConfidenceInterval()

      val mean = Statistic mean this
      log.verbose("--Average--            " + (mean formatted "%.2f"))

      val (left, right) = Statistic confidenceInterval this
      log.verbose("--Confident Interval-- [" + (left formatted "%.2f") + "; " +
        (right formatted "%.2f") + "]")

      var diff = (right - left)
      log.verbose("--Difference--         " + (diff formatted "%.2f") + " = " +
        ((diff / mean * 100) formatted "%.2f") + "%")

      while (Statistic.isConfidenceLevelAcceptable && (diff / mean) >= Constant.CI_PRECISION_THREDSHOLD) {
        Statistic.reduceConfidenceLevel()

        val (left, right) = Statistic confidenceInterval this
        log.verbose("--Confident Interval-- [" + (left formatted "%.2f") + "; " +
          (right formatted "%.2f") + "]")

        diff = (right - left)
        log.verbose("--Difference--         " + (diff formatted "%.2f") + " = " +
          ((diff / mean * 100) formatted "%.2f") + "%")
      }

      if ((diff / mean) < Constant.CI_PRECISION_THREDSHOLD) {
        this.confidenceLevel = Statistic.confidenceLevel.toInt
        Statistic.resetConfidenceInterval()
        true
      } else {
        Statistic.resetConfidenceInterval()
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
          this += line.toLong
        }
      } catch {
        case e => {
          log.debug("[Read failed] " + file.path + e.toString)
          this.clear()
        }
      }
    }
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store(): Option[File] = {
    if (array.length == 0) {
      log("Nothing to store")
      return None
    }
    val data = new ArrayBuffer[String]
    data += "Date:             " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm:ss").format(new Date)
    data += "Main Class:       " + benchmark.name
    data += "Type:             " + config.benchmarkType
    data += "Confidence level: " + confidenceLevel + " %"
    data += "-------------------------------"

    FileUtil.createAndStore(
      config.persistorLocation.path,
      benchmark.name + "." + config.benchmarkType,
      foldLeft(data) { (data, l) => data + l.toString }
    )
  }

  /**
   *
   */
  override def toString(): String = {
    val endl = (System getProperty "line.separator")
    foldLeft("Benchmarking result at " + confidenceLevel.toString() + "%: ") {
      (str, l) =>
        str + endl + "             ----  " + l
    }
  }

}
