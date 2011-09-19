/*
 * ArrayBufferPersistor
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.MeasurerFactory
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class ArrayBufferPersistor(log: Log, config: Config, benchmark: Benchmark) extends Persistor with SimpleFilePersistor {

  private var data: ArrayBuffer[Series] = null

  private var _location: Directory = null
  def location() = _location

  def this(log: Log, config: Config, benchmark: Benchmark, location: Directory, data: ArrayBuffer[Series]) {
    this(log, config, benchmark)
    this._location = location
    this.data = data
  }

  /**
   * Add a `Series` to `data`.
   */
  def add(ele: Series) = {
    data += ele
    this
  }

  def apply(i: Int) = data(i)

  def foldLeft[B](z: B)(op: (B, Series) => B): B = data.foldLeft[B](z)(op)

  def head: Series = data.head

  def last = data.last

  def tail = new ArrayBufferPersistor(log, config, benchmark, location, data.tail)

  def length = data.length

  def foreach(f: Series => Unit): Unit = data foreach f

  def forall(op: Series => Boolean) = data forall op

  /**
   * Generates sample results.
   */
  def generate(mode: BenchmarkMode, num: Int): Persistor = {
    var i = 0
    val measurer = new MeasurerFactory(log, config) create mode
    while (i < num) {
      measurer run benchmark match {
        case success: MeasurementSuccess => {

          val storer = new LoadStoreManagerFactory(log, config).create(benchmark, this, mode)
          storer.storeMeasurementResult(success, BenchmarkSuccess(success.series.confidenceLevel, success)) match {
            case Some(_) => {
              log.debug("--Stored--")
              i += 1
              log.verbose("--Got " + i + " sample(s)--")
            }
            case _ => {
              log.debug("--Cannot store--")
            }
          }
          this add success.series
        }
        case failure: MeasurementFailure => {
          log.debug("--Generation error at " + i + ": " + failure.reason + "--")
        }
      }
    }
    this
  }

}
