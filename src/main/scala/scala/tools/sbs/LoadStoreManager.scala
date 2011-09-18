package scala.tools.sbs

import scala.tools.nsc.io.File
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.regression.Persistor

trait LoadStoreManager {

  def loadPersistor(): Persistor

  def storeMeasurementResult(result: MeasurementResult): Option[File]

}
