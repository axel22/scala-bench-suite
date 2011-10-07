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
  private var _classes = ArrayBuffer[LoadedClass]()

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

  /** Number of garbage collection cycle.
   */
  private var _gcCycle = 0

  def gcCycle = _gcCycle

  def gc {
    _gcCycle += 1
  }

  def toXML =
    <profile>
      <classes>{ for (clazz <- classes) yield clazz.toXML }</classes>
      <boxing>{ boxing }</boxing>
      <unboxing>{ unboxing }</unboxing>
      <gc>{ gcCycle }</gc>
    </profile>

}
