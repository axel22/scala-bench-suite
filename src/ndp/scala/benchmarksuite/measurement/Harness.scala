/*
 * Harness
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.measurement

import ndp.scala.benchmarksuite.regression.BenchmarkResult
import ndp.scala.benchmarksuite.utility.Config
import ndp.scala.benchmarksuite.utility.Log

/**
 * Abstract base class for iterating and measuring the running time of the benchmark classes.
 *
 * @author ND P
 */
abstract class Harness(log: Log, config: Config) {

  /**
   * Does the warm up and measure metric of the benchmark classes.
   */
  def run(): BenchmarkResult

}
