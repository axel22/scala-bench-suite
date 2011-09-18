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

import scala.annotation.implicitNotFound
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.regression.StatisticFactory
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Constant
import scala.tools.sbs.util.Log

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
   * The confidence level that at which, this series' confidence interval is not greater than
   * 2% of its mean.
   */
  private var _confidenceLevel: Int = 0
  def confidenceLevel = _confidenceLevel

  def this(log: Log, config: Config, series: ArrayBuffer[Long], confidenceLevel: Int) {
    this(log, config)
    data = series
    _confidenceLevel = confidenceLevel
  }

  def head = data.head

  def tail = new ArrayBufferSeries(log, config, data.tail, _confidenceLevel)

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
      val statistic = new StatisticFactory(log, config) create 0

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
        _confidenceLevel = statistic.confidenceLevel.toInt
        true
      } else {
        false
      }
    }
  }

  /**
   *
   */
  override def toString(): String =
    foldLeft("Benchmarking result at " + confidenceLevel + "%: ") { (str, l) => str + "--" + l }

}
