/*
 * ResultPack
 * 
 * Version
 * 
 * Created September 27th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs

import scala.collection.mutable.ArrayBuffer

/** Holds all the benchmarking results from one sbs running
 */
class ResultPack {

  private var results = ArrayBuffer[BenchmarkResult]()

  def add(newResult: BenchmarkResult) {
    results += newResult
  }

  def total = results.length

  def ok = successes.length

  def failed = total - ok

  def foreach(f: BenchmarkResult => Unit) = results foreach f

  def successes = results filter (_.isInstanceOf[BenchmarkSuccess])

  def failures = results filter (_.isInstanceOf[BenchmarkFailure])

}
