package ndp.scala.tools

import ndp.scala.tools.sbs.util.Config
import ndp.scala.tools.sbs.util.Log

package object sbs {

  private var _log: Log = null
  def log = _log
  def log_=(log: Log) {
    _log = log
  }

  private var _config: Config = null
  def config = _config
  def config_=(config: Config) {
    _config = config
  }
  
  private var _benchmark: Benchmark = null
  def benchmark = _benchmark
  def benchmark_=(benchmark: Benchmark) {
    _benchmark = benchmark
  }

}