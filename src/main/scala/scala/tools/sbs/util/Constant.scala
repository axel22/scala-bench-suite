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

import java.lang.System

/** Utilities for benchmarking.
 */
object Constant {

  val COLON = System getProperty "path.separator"

  val SLASH = System getProperty "file.separator"

  val ENDL = System getProperty "line.separator"

  /** Precision thredshold of confidance interval.
   */
  val CI_PRECISION_THREDSHOLD = 0.02

  /** Thredshold to detect whether the benchmark has reached steady state.
   */
  val STEADY_THREDSHOLD = 0.02

  /** Maximum counter for measurements of a series.
   */
  val MAX_MEASUREMENT = 1

}
