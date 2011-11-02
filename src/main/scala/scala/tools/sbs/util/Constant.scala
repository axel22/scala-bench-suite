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

  val INDENT = "         "

  val DOLLAR = "$"

  val COMPANION_FIELD = "MODULE$"

  /** Smallest acceptable confidence level.
   */
  val LEAST_CONFIDENCE_LEVEL = 90

  /** Precision thredshold of confidance interval.
   */
  val CI_PRECISION_THRESHOLD = 0.02

  /** Thredshold to detect whether the benchmark has reached steady state.
   */
  val STEADY_THRESHOLD = 0.02

  /** Maximum counter for measurements of a series.
   */
  val MAX_MEASUREMENT = 1

  /** Maximum multiplier for waming up.
   */
  val MAX_WARM = 5

}
