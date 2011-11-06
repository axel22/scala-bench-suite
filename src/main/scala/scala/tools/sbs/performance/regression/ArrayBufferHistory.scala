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
package performance
package regression

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.io.Log

/** An implement of {@link History}. Uses `ArrayBuffer` to hold previous measurement data.
 */
class ArrayBufferHistory(log: Log, config: Config, benchmark: Benchmark, mode: BenchmarkMode) extends History {

  /** The {@link Series} array.
   */
  private var data = ArrayBuffer[Series]()

  def this(log: Log,
           config: Config,
           benchmark: Benchmark,
           mode: BenchmarkMode,
           data: ArrayBuffer[Series]) {
    this(log, config, benchmark, mode)
    this.data = data
  }

  /** Benchmarking mode of the history.
   */
  def mode(): BenchmarkMode = mode

  /** Adds a `Series` to `data`.
   */
  def add(ele: Series) = {
    data += ele
    this
  }

  /** Appends a `History` to `this`.
   */
  def append(tag: History) {
    tag foreach (this add _)
    this
  }

  def apply(i: Int) = data(i)

  def foldLeft[B](z: B)(op: (B, Series) => B): B = data.foldLeft[B](z)(op)

  def head: Series = data.head

  def last = data.last

  def tail = new ArrayBufferHistory(log, config, benchmark, mode, data.tail)

  def +:(elem: Series) = new ArrayBufferHistory(log, config, benchmark, mode, data :+ elem)

  def length = data.length

  def map[B, That](f: Series => B)(implicit bf: CanBuildFrom[ArrayBuffer[Series], B, That]): That = data map f

  def foreach(f: Series => Unit): Unit = data foreach f

  def forall(op: Series => Boolean) = data forall op

}
