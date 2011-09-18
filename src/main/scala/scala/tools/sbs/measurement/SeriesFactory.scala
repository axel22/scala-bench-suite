package scala.tools.sbs
package measurement
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log
import scala.collection.mutable.ArrayBuffer

class SeriesFactory(log: Log, config: Config) {

  def create(): Series = new ArrayBufferSeries(log, config)

  def create(series: ArrayBuffer[Long], confidenceLevel: Int): Series =
    new ArrayBufferSeries(log, config, series, confidenceLevel)

}
