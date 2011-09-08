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

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.io.Source.fromFile
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.FileUtil
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.regression.Statistic
import ndp.scala.tools.sbs.util.Constant

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class BenchmarkResult extends ArrayBuffer[Long] {

  /**
   * Calculates statistical metrics.
   *
   * @return
   * <ul>
   * <li>`true` if the ration between the confidence interval and the mean is less than the thredshold
   * <li>`false` otherwise
   * </ul> 
   */
  def isReliable = {

    if (length == 0) {
      log.debug("Init measurement")
      false
    } else if (length != config.multiplier) {
      log.debug("Wrong in measurment length")
      false
    } else {
      val mean = Statistic mean this
      val (left, right) = Statistic confidenceInterval this
      val diff = (right - left)

      for (i <- array) {
        log.debug("[Measured]	" + i)
      }
      log.verbose("[Average]	            " + (mean formatted "%.2f"))
      log.verbose("[Confident Interval]	[" + (left formatted "%.2f") + "; " + (right formatted "%.2f") + "]")
      log.verbose("[Difference]           " + (diff formatted "%.2f") + " = " + ((diff / mean * 100) formatted "%.2f") + "%")
      (diff / mean) < Constant.CONFIDENCE_INTERVAL_PRECISION_THREDSHOLD
    }
  }
  
  /**
   *
   */
  def load(file: File) {
    for (line <- fromFile(file.getAbsolutePath) getLines) {
      try {
        if (line startsWith "Date") {

        } else if (line startsWith "-") {

        } else if (line startsWith "Type") {

        } else if (line startsWith "Main") {

        } else {
          this += line.toLong
        }
      } catch {
        case e => {
          log(e.toString)
          throw new Exception("In file " + file.getName + ": " + line)
        }
      }
    }
  }

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store() {
    if (array.length == 0) {
      log("Nothing to store")
      return
    }
    log verbose this.toString
    log verbose "Stored to " + FileUtil.storeResult(log, config, this)
  }

  /**
   *
   */
  override def toString(): String = {
    var str = "Benchmarking result:" + (System getProperty "line.separator")
    for (l <- array) {
      str += (System getProperty "line.separator") + "                " + l
    }
    str
  }
}
