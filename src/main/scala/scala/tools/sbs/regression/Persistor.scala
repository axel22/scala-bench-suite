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

import scala.tools.sbs.benchmark.BenchmarkMode.BenchmarkMode
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.Series

trait Persistor {

  def add(ele: Series): Unit

  def mode: BenchmarkMode

  def concat(that: Persistor): Persistor

  def apply(i: Int): Series

  def foldLeft[B](z: B)(op: (B, Series) => B): B

  def head: Series

  def last: Series

  def tail: Persistor

  def length: Int

  def foreach(f: Series => Unit): Unit

  def forall(op: Series => Boolean): Boolean

  def generate(num: Int): Persistor

  def load(): Persistor

  def store(measurementSuccess: MeasurementSuccess, result: BenchmarkResult): Boolean

}
