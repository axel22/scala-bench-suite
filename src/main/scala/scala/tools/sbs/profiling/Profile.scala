/*
 * Profile
 * 
 * Version
 * 
 * Created on October 2nd, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package profiling

import scala.collection.mutable.ArrayBuffer

/** Class holds the profiling result.
 */
class Profile {

  /** All classes loaded in a benchmark running.
   */
  private val _classes = ArrayBuffer[LoadedClass]()

  def classes = _classes

  def loadClass(name: String) {
    _classes += LoadedClass(name)
  }

  def loadClass(clazz: LoadedClass) {
    _classes += clazz
  }

  /** Number of boxing.
   */
  private var _boxing = 0

  def boxing = _boxing

  def box {
    _boxing += 1
  }

  /** Number of unboxing.
   */
  private var _unboxing = 0

  def unboxing = _unboxing

  def unbox {
    _unboxing += 1
  }

  /** Number of steps performed.
   */
  private var _steps = 0

  def steps = _steps

  def performStep {
    _steps += 1
  }

  /** Garbage collectors' cycles.
   */
  private val _garbageCollections = ArrayBuffer[GarbageCollection]()

  def garbageCollections = _garbageCollections

  def gc(name: String, cycle: Int, time: Int) {
    _garbageCollections += GarbageCollection(name, cycle, time)
  }

  def toXML =
    <profile>
      <classes>{ for (clazz <- classes) yield clazz.toXML }</classes>
      <steps>{ steps }</steps>
      <boxing>{ boxing }</boxing>
      <unboxing>{ unboxing }</unboxing>
      <garbagecollection>{ for (gc <- garbageCollections) yield gc.toXML }</garbagecollection>
    </profile>

}
