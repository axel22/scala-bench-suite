/*
 * LoadStoreManager
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.nsc.io.File
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.regression.BenchmarkResult
import scala.tools.sbs.regression.Persistor

trait LoadStoreManager {

  def loadPersistor(): Persistor

  def storeMeasurementResult(result: MeasurementSuccess, regression: BenchmarkResult): Option[File]

}
