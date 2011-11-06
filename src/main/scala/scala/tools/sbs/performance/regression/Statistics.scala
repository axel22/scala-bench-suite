/*
 * Statistic
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs
package performance
package regression

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log

/** Class stores the significant level and computes statistical arguments for a given sample.
 */
trait Statistics {

  def reduceConfidenceLevel(): Int

  def isConfidenceLevelAcceptable: Boolean

  def resetConfidenceInterval(): Unit

  def confidenceInterval(series: Series): (Double, Double)

  def min(series: Series): Long

  def max(series: Series): Long

  def mean(series: Series): Double

  def standardDeviation(series: Series): Double

  /** Coefficient of variation
   */
  def CoV(series: Series): Double

  def significantLevel: Double

  def confidenceLevel: Int

  def testDifference(benchmark: Benchmark,
                     current: Series,
                     history: History): RegressionResult

}

object StatisticsFactory {

  def apply(config: Config, log: Log, alpha: Double = 0): Statistics = {
    new SimpleStatistics(config, log, alpha)
  }

}
