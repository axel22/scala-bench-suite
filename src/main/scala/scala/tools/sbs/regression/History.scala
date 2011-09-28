/*
 * History
 * 
 * Version
 * 
 * Created on September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.annotation.implicitNotFound
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.Series

import BenchmarkMode.BenchmarkMode

trait History {

  def add(ele: Series): History

  def append(tag: History): Unit

  def mode: BenchmarkMode

  def concat(that: History): History

  def apply(i: Int): Series

  def foldLeft[B](z: B)(op: (B, Series) => B): B

  def head: Series

  def last: Series

  def tail: History

  def length: Int

  def foreach(f: Series => Unit): Unit

  def forall(op: Series => Boolean): Boolean

}

object HistoryFactory {

  def apply(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode): History =
    new ArrayBufferHistory(log, config, benchmark, mode)

}
