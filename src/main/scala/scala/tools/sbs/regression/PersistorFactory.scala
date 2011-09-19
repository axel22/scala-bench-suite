/*
 * PersistorFactory
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
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.util.Config
import scala.tools.sbs.util.Log

class PersistorFactory(log: Log, config: Config) {

  def create(benchmark: Benchmark, location: Directory): Persistor = {
    new ArrayBufferPersistor(log, config, benchmark, location, ArrayBuffer[Series]())
  }

  def create(benchmark: Benchmark, location: Directory, data: ArrayBuffer[Series]): Persistor = {
    new ArrayBufferPersistor(log, config, benchmark, location, data)
  }

}
