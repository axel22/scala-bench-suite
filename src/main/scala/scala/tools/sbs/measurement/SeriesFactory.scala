/*
 * SeriesFactory
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class SeriesFactory(log: Log, config: Config) {

  def create(): Series = new ArrayBufferSeries(log, config)

  def create(series: ArrayBuffer[Long], confidenceLevel: Int): Series =
    new ArrayBufferSeries(log, config, series, confidenceLevel)

}
