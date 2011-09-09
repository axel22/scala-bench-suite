/*
 * Constant
 * 
 * Version 
 * 
 * Created on September 5th, 2011
 *
 * Created by ND P
 */

package ndp.scala.tools.sbs
package util

/**
 * Utilities for benchmarking.
 */
object Constant {

  /**
   * Result of regression.
   */
  val REGRESSION_PASS = true
  /**
   * Result of regression.
   */
  val REGRESSION_FAILED = false
  
  /**
   * Precision thredshold of confidance interval.
   */
  val CONFIDENCE_INTERVAL_PRECISION_THREDSHOLD = 0.02
  
  /* Constants for creating command line arguments to rebuild Config */
  /**
   * Maximum counter for measurements of a series.
   */
  val MAX_MEASUREMENT = 10

  val MAX_ARGUMENT_CONFIG = 13
  val INDEX_CLASSNAME = 0
  val INDEX_SRCPATH = 1
  val INDEX_BENCHMARK_DIR = 2
  val INDEX_BENCHMARK_BUILD = 3
  val INDEX_BENCHMARK_TYPE = 4
  val INDEX_RUNS = 5
  val INDEX_MULTIPLIER = 6
  val INDEX_SCALA_HOME = 7
  val INDEX_JAVA_HOME = 8
  val INDEX_CLASSPATH = 9
  val INDEX_PERSISTOR_LOC = 10
  val INDEX_SAMPLE_NUMBER = 11
  val INDEX_COMPILE = 12

  /* Constatns for creating command line arguments to rebuild Log */
  val MAX_ARGUMENT_LOG = 3
  
  val INDEX_LOG_FILE = 0
  val INDEX_LOG_LEVEL = 1
  val INDEX_SHOW_LOG = 2

}
