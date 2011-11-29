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

/** Holds all the benchmarking results from one sbs running.
 */
class ResultPack {

  private var modes = ArrayBuffer[ReportMode](new ReportMode(DummyMode))

  def switchMode(mode: BenchmarkMode) = modes :+= new ReportMode(mode)

  private def currentMode = modes.last

  def add(newResult: BenchmarkResult) = currentMode add newResult

  def total = modes./:(0)((total, mode) => total + mode.results.length)

  def ok = success.length

  def failed = total - ok

  def foreach(mode: ReportMode => Unit) = modes foreach mode

  def success = modes./:(ArrayBuffer[BenchmarkResult]())((arr, mode) => arr ++ mode.success)

  def failure = modes./:(ArrayBuffer[BenchmarkResult]())((arr, mode) => arr ++ mode.failure)

}

class ReportMode(mode: BenchmarkMode) {

  private var _results = ArrayBuffer[BenchmarkResult]()

  def add(newResult: BenchmarkResult) = _results += newResult

  def results = _results

  def foreach(f: BenchmarkResult => Unit) = results foreach f

  def success = results filterNot (failure contains _)

  def failure = results filter (_.isInstanceOf[BenchmarkFailure])

  def toReport = "[" + mode.description + "]"

}
