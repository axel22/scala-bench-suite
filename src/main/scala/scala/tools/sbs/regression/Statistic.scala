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
package regression

import scala.collection.mutable.ArrayBuffer
import scala.math.sqrt
import org.apache.commons.math.distribution.FDistributionImpl
import org.apache.commons.math.distribution.NormalDistributionImpl
import org.apache.commons.math.distribution.TDistributionImpl
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementSuccess

/**
 * Class stores the significant level and computes statistical arguments for a given sample.
 */
trait Statistic {

  def reduceConfidenceLevel(): Int

  def isConfidenceLevelAcceptable: Boolean

  def resetConfidenceInterval(): Unit

  def confidenceInterval(series: Series): (Double, Double)

  def min(series: Series): Long

  def max(series: Series): Long

  def mean(series: Series): Double

  def standardDeviation(series: Series): Double

  def CoV(series: Series): Double

  def significantLevel: Double

  def confidenceLevel: Int

  def testDifference(measurementResult: MeasurementSuccess, persistor: Persistor): BenchmarkResult

}
