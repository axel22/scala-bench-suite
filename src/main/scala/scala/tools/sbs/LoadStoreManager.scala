package scala.tools.sbs

import scala.tools.sbs.regression.Persistor
import scala.tools.nsc.io.Directory
import scala.tools.sbs.measurement.MeasurementResult

trait LoadStoreManager {

  def loadPersistor(): Persistor

  def storeMeasurementResult(r: MeasurementResult)

}
