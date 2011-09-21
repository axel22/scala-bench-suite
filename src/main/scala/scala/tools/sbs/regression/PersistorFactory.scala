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
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode

class PersistorFactory(log: Log, config: Config) {

  def create(benchmark: Benchmark, mode: BenchmarkMode): Persistor = {
    new ArrayBufferPersistor(log, config, benchmark, mode: BenchmarkMode, ArrayBuffer[Series]())
  }

  def create(benchmark: Benchmark, mode: BenchmarkMode, data: ArrayBuffer[Series]): Persistor = {
    new ArrayBufferPersistor(log, config, benchmark, mode: BenchmarkMode, data)
  }

}
