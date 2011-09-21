/*
 * SimpleFilePersistor
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.tools.nsc.io.Directory
import scala.tools.nsc.io.File
import scala.tools.sbs.measurement.MeasurementSuccess

trait FilePersistor {

  def location(): Directory

  def loadFromFile(): FilePersistor

  def storeToFile(measurement: MeasurementSuccess, result: BenchmarkResult): Option[File]

}
