/*
 * ArrayBufferHistory
 * 
 * Version
 * 
 * Created on September 25th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package regression

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.io.Log
import scala.tools.sbs.measurement.Series

class ArrayBufferHistory(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode) extends History {

  private var data = ArrayBuffer[Series]()

  def this(log: Log,
           config: Config,
           benchmark: Benchmark,
           mode: BenchmarkMode,
           data: ArrayBuffer[Series]) {
    this(log, config, benchmark, mode)
    this.data = data
  }

  def mode(): BenchmarkMode = mode

  /** Add a `Series` to `data`.
   */
  def add(ele: Series) = {
    data += ele
    this
  }

  def append(tag: History) {
    tag foreach (this add _)
    this
  }

  def concat(that: History): History = {
    that foreach (this add _)
    this
  }

  def apply(i: Int) = data(i)

  def foldLeft[B](z: B)(op: (B, Series) => B): B = data.foldLeft[B](z)(op)

  def head: Series = data.head

  def last = data.last

  def tail = new ArrayBufferHistory(log, config, benchmark, mode, data.tail)

  def length = data.length

  def map[B, That](f: Series => B)(implicit bf: CanBuildFrom[ArrayBuffer[Series], B, That]): That = data map f

  def foreach(f: Series => Unit): Unit = data foreach f

  def forall(op: Series => Boolean) = data forall op

}
