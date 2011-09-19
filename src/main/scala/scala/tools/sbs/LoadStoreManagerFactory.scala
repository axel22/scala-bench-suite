/*
 * LoadStoreManagerFactory
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.regression.Persistor
import scala.tools.sbs.regression.SimpleFilePersistor
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class LoadStoreManagerFactory(log: Log, config: Config) {
  
  def create(benchmark: Benchmark, persistor: Persistor, mode: BenchmarkMode): LoadStoreManager = {
    persistor match {
      case sfp: SimpleFilePersistor => new SimpleLoadStoreManager(log, config, benchmark, sfp.location, mode)
      case _ => null
    }
  }

}