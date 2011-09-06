/*
 * Regression
 * 
 * Version
 * 
 * Created on September 5th 2011
 * 
 * Created by ND P
 */

package ndp.scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer

import ndp.scala.tools.sbs.measurement.BenchmarkResult
import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Constant
import ndp.scala.tools.sbs.util.Log
import ndp.scala.tools.sbs.util.LogLevel
import ndp.scala.tools.sbs.util.Report

object Regression {
  /**
   * Loads benchmark histories from files and uses <code>Statistic</code> class to detect regression.
   */
  def detectRegression(result: BenchmarkResult) {

    val report = new Report
    val persistor = new Persistor
    var storedResult: BenchmarkResult = null
    var line: String = null

    persistor += result
    persistor.load

    if (Statistic testDifference persistor) {
      val means: ArrayBuffer[Double] = new ArrayBuffer[Double]
      log.debug(persistor.toString())
      for (i <- persistor) {
        means += Statistic mean i
      }
      log.debug(persistor.toString())
      report(log, config, Constant.FAILED, Report dueToRegression means)
    } else {
      report(log, config, Constant.PASS, null)
    }
  }

}
