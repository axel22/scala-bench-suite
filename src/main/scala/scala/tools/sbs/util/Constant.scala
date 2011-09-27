/*
 * Constant
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package scala.tools.sbs
package util

/** Utilities for benchmarking.
 */
object Constant {
  
  val COLON = System getProperty "path.separator"
  
  val SLASH = System getProperty "file.Separator"

  /** Precision thredshold of confidance interval.
   */
  val CI_PRECISION_THREDSHOLD = 0.02

  /** Thredshold to detect whether the benchmark has reached steady state.
   */
  val STEADY_THREDSHOLD = 0.02

  /* Constants for creating command line arguments to rebuild Config */
  /** Maximum counter for measurements of a series.
   */
  val MAX_MEASUREMENT = 1

}
