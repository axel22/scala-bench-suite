/*
 * InfoPack
 * 
 * Version
 * 
 * Created November 10th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package benchmark

import scala.collection.mutable.ArrayBuffer

/** Holds infomation about benchmarking modes and benchmarks.
 */
class InfoPack {

  private var modes = ArrayBuffer[InfoMode]()

  def switchMode(mode: BenchmarkMode) = modes :+= new InfoMode(mode)

  def apply(mode: BenchmarkMode) = modes find (_.mode == mode) get

  private def currentMode = modes.last

  def add(newInfo: BenchmarkInfo) = currentMode add newInfo

  def foreach(f: InfoMode => Unit) = modes foreach f

  def filter(f: BenchmarkInfo => Boolean): InfoPack = {
    val pack = new InfoPack
    modes foreach (mode => {
      pack switchMode mode.mode
      mode.infos filter (f(_)) foreach (pack add _)
    })
    pack
  }
  
  def filterNot(f: BenchmarkInfo => Boolean): InfoPack = {
    val pack = new InfoPack
    modes foreach (mode => {
      pack switchMode mode.mode
      mode.infos filterNot (f(_)) foreach (pack add _)
    })
    pack
  }

  def contains(info: BenchmarkInfo) = modes exists (_.infos contains info)

}

class InfoMode(val mode: BenchmarkMode) {

  private var _infos = ArrayBuffer[BenchmarkInfo]()

  def add(newInfo: BenchmarkInfo) = _infos += newInfo

  def infos = _infos

  def foreach(f: BenchmarkInfo => Unit) = infos foreach f

  def map[B](f: BenchmarkInfo => B) = infos map f

}
