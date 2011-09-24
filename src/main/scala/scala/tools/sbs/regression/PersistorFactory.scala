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
import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.Series

object PersistorFactory {

  def apply(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode): Persistor =
    new ArrayBufferPersistor(log, config, benchmark, mode: BenchmarkMode, ArrayBuffer[Series]())

  def apply(
    log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode, data: ArrayBuffer[Series]): Persistor =
    new ArrayBufferPersistor(log, config, benchmark, mode: BenchmarkMode, data)

}
