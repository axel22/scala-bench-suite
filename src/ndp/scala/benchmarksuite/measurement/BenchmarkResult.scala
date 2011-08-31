/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on August 11th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite
package measurement

import scala.collection.mutable.ArrayBuffer

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class BenchmarkResult extends ArrayBuffer[Long] {

  /*/**
   * The name of the file contains the benchmarking result.
   */
  private var _filename: String = null
  def filename = _filename
  def filename_=(name: String) {
    _filename = name
  }
  /**
   * The <code>List[Long]</code> represent the result value series.
   */
//  private var _series: ArrayBuffer[Long] = null
//  def series = _series
//  def series_=(series: ArrayBuffer[Long]) {
//    _series = series
//  }
  /**
   * The name of the benchmark class
   */
  private var _classname: String = null
  def classname = _classname
  def classname_=(classname: String) {
    _classname = classname
  }
  /**
   * The type of benchmarking (performance or memory consumption).
   * <ul>
   * <li><code>true</code> value for performance benchmarking
   * <li><code>false</code> value for memory consumption benchmarking
   * </ul>
   */
  private var _benchmarkType: BenchmarkType.Value = BenchmarkType.Memory
  def benchmarkType = _benchmarkType
  def benchmarkType_=(benchmarkType: BenchmarkType.Value) {
    _benchmarkType = benchmarkType
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given filename.
   *
   * @param theFileName	The name of the file used for storing.
   */
  def this(theFileName: String) {
    this
    filename = theFileName
  }
  
  /**
   * Constructs a <code>BenchmarkResult</code> using the given class name, filename and benchmarking type.
   *
   * @param theFileName	The name of the file used for storing.
   * @param theClassname	The name of the benchmark class.
   * @param theBenchmarkType	The type of benchmarking.
   */
  def this(theFileName: String, theClassname: String, theBenchmarkType: BenchmarkType.Value) {
    this(theFileName)
    classname = theClassname
    benchmarkType = theBenchmarkType
  }*/
}