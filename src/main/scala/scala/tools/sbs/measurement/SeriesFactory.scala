package scala.tools.sbs.measurement
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class SeriesFactory(log: Log, config: Config) {

  def create(): Series = {
    new ArrayBufferSeries(log, config)
  }

}
