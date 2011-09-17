/*
 * Series
 * 
 * Version
 * 
 * Created on September 17th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package measurement

import scala.tools.nsc.io.File

trait Series {

  /**
   *
   */
  def confidenceLevel: Int


  def head: Long

  def tail: Series

  def last: Long

  def length: Int

  def clear()

  def +=(ele: Long): Series

  def foldLeft[B](z: B)(op: (B, Long) => B): B

  def foldRight[B](z: B)(op: (Long, B) => B): B

  def forall(op: Long => Boolean): Boolean

  def remove(n: Int): Long

  /**
   * Calculates statistical metrics.
   *
   * @return
   * <ul>
   * <li>`true` if the ration between the confidence interval and the mean is less than the thredshold
   * <li>`false` otherwise
   * </ul>
   */
  def isReliable: Boolean
  
  /**
   *
   */
  def load(file: File)

  /**
   * Stores result value series in to text files whose name is the default name
   * in the format: YYYYMMDD.hhmmss.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def store(passed: Boolean): Option[File]

  /**
   *
   */
  override def toString(): String

}