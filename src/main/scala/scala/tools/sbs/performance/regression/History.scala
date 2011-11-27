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
package performance
package regression

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.performance.Series

trait History {

  def add(ele: Series): History

  def append(tag: History): Unit

  def mode: BenchmarkMode

  def apply(i: Int): Series

  def foldLeft[B](z: B)(op: (B, Series) => B): B

  def head: Series

  def last: Series

  def tail: History

  def +:(elem: Series): History

  def length: Int

  def map[B, That](f: Series => B)(implicit bf: CanBuildFrom[ArrayBuffer[Series], B, That]): That

  def foreach(f: Series => Unit): Unit

  def forall(op: Series => Boolean): Boolean

}

object HistoryFactory {

  def apply(benchmark: Benchmark, mode: BenchmarkMode): History =
    new ArrayBufferHistory(benchmark, mode)

}
