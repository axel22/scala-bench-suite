/*
 * Series
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package performance

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.performance.regression.StatisticsFactory
import scala.tools.sbs.util.Constant.ENDL

/** Class represents the result of a success measurement.
 */
class Series(config: Config, log: Log) {

  /** The value series.
   */
  private var data = ArrayBuffer[Long]()

  /** The confidence level that at which, this series' confidence interval is not greater than
   *  2% of its mean.
   */
  private var _confidenceLevel: Int = 100
  def confidenceLevel = _confidenceLevel

  def this(config: Config, log: Log, series: ArrayBuffer[Long], confidenceLevel: Int) {
    this(config, log)
    data = series
    _confidenceLevel = confidenceLevel
  }

  def apply(idx: Int) = data.apply(idx)

  def head = data.head

  def tail = new Series(config, log, data.tail, _confidenceLevel)

  def last = data.last

  def length = data.length

  def clear() = data.clear()

  def +=(ele: Long) = {
    data += ele
    this
  }

  def sum = data.sum

  def foldLeft[B](z: B)(op: (B, Long) => B) = data.foldLeft[B](z)(op)

  def foldRight[B](z: B)(op: (Long, B) => B) = data.foldRight[B](z)(op)

  def foreach[U](f: Long => Unit): Unit = data foreach f

  def forall(op: Long => Boolean) = data forall op

  def remove(n: Int) = data remove n

  /** Calculates statistical metrics.
   *
   *  @return
   *  <ul>
   *  <li>`true` if the ration between the confidence interval and the mean is less than the thredshold
   *  <li>`false` otherwise
   *  </ul>
   */
  def isReliable: Boolean = {

    if (data.length == 0) {
      log.debug("  Cleared result  " + ENDL)
      UI.debug("  Cleared result  " + ENDL)
      false
    }
    else {
      val statistic = StatisticsFactory(config, log)

      val mean = statistic mean this
      var (left, right) = statistic confidenceInterval this
      var diff = right - left

      def formattedCI = "[" + (left formatted "%.2f") + "; " + (right formatted "%.2f") + "]"
      def formattedDiff =
        (diff formatted "%.2f") + " ~ " + (if (diff != 0) (diff / mean * 100) formatted "%.2f" else "0.00") + "%"

      def toPrint = "  At confidence level  " + statistic.confidenceLevel + "%:" + ENDL + (
        if (!statistic.isConfidenceLevelAcceptable) "               Unacceptable"
        else "               Confident Interval " + formattedCI + ENDL +
          "               Difference         " + formattedDiff)

      log.verbose(toPrint)
      UI.verbose(toPrint)

      def ratio = if (diff == 0) 0 else diff / mean
      def checkCL = {
        statistic.reduceConfidenceLevel()
        statistic.isConfidenceLevelAcceptable
      }
      while (ratio >= config.precisionThreshold && checkCL) {

        val ci = statistic confidenceInterval this
        left = ci._1
        right = ci._2
        diff = right - left

        log.verbose(toPrint)
        UI.verbose(toPrint)
      }

      log.info("Series: " + this.toString)
      UI.verbose("Series: " + this.toString)
      log.info("  Average              " + (mean formatted "%.2f"))
      UI.info("  Average              " + (mean formatted "%.2f"))
      log.info(toPrint)
      log.info("")
      UI.info(toPrint)
      UI.info("")

      _confidenceLevel = statistic.confidenceLevel.toInt
      ratio < config.precisionThreshold
    }
  }

  /**
   */
  override def toString(): String =
    data.foldLeft("At " + confidenceLevel + "%: ") { (str, l) => str + "[" + l + "] " }

  /** All of the xml should be on only one line for parsing from sub process.
   */
  def toXML =
    <Series>
      <confidenceLevel>{ confidenceLevel }</confidenceLevel>
      <data>{ for (l <- data) yield <value>{ l.toString }</value> }</data>
    </Series>

}
