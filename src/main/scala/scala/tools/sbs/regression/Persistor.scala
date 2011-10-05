/*
 * Persistor
 * 
 * Version
 * 
 * Created on September 5th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.tools.sbs.common.Benchmark
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.MeasurementSuccess

/** Trait for loading, storing and generating benchmarking history.
 */
trait Persistor {

  def generate(num: Int): History

  def load(): History

  def store(measurementSuccess: MeasurementSuccess, result: RegressionResult): Boolean

}

/** Factory object of {@link Persistor}.
 */
object PersistorFactory {

  def apply(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode): Persistor =
    new FileBasedPersistor(log, config, benchmark, mode)

}
