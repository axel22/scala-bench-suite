/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on August 11th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.utility

import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date

import scala.io.Source.fromFile
import scala.collection.mutable.ArrayBuffer

/**
 * Class represents the result of benchmarking. Allows user to store or load a list of values from file.
 *
 * @author ND P
 */
class BenchmarkResult {

  /**
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
  private var _series: ArrayBuffer[Long] = null
  def series = _series
  def series_=(series: ArrayBuffer[Long]) {
    _series = series
  }
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
  private var _benchmarkType: Boolean = true
  def benchmarkType = _benchmarkType
  def benchmarkType_=(benchmarkType: Boolean) {
    _benchmarkType = benchmarkType
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given file name.
   *
   * @param theFilename	The name of the file used for loading or storing.
   */
  def this(theFilename: String) {
    this
    filename = theFilename
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given value series.
   *
   * @param theSeries	The result value series of a benchmarking to be stored.
   */
  def this(theSeries: ArrayBuffer[Long]) {
    this
    series = theSeries
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given class name and value series.
   *
   * @param theSeries	The result value series of a benchmarking to be stored.
   * @param theClassname	The name of the benchmark class.
   */
  def this(theSeries: ArrayBuffer[Long], theClassname: String) {
    this(theSeries)
    classname = theClassname
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given class name, value series and benchmarking type.
   *
   * @param theSeries	The result value series of a benchmarking to be stored.
   * @param theClassname	The name of the benchmark class.
   * @param theBenchmarkType	The type of benchmarking.
   */
  def this(theSeries: ArrayBuffer[Long], theClassname: String, theBenchmarkType: Boolean) {
    this(theSeries, theClassname)
    benchmarkType = theBenchmarkType
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given file name and value series.
   *
   * @param theFilename	The name of the file used for storing.
   * @param theSeries	The result value series of a benchmarking to be stored.
   */
  def this(theFilename: String, theSeries: ArrayBuffer[Long]) {
    this(theFilename)
    series = theSeries
  }

  /**
   * Constructs a <code>BenchmarkResult</code> using the given informations.
   *
   * @param theFilename	The name of the file used for storing.
   * @param theSeries	The result value series of a benchmarking to be stored.
   * @param theClassname	The name of the benchmark class.
   */
  def this(theFilename: String, theSeries: ArrayBuffer[Long], theClassname: String, theBenchmarkType: Boolean) {
    this(theSeries, theClassname)
    filename = theFilename
    benchmarkType = theBenchmarkType
  }

  /**
   * Loads a result value series from file whose name is in the field <code>Filename</code>.
   * @return	The result value series
   */
  def load(): ArrayBuffer[Long] = {
    for (line <- fromFile(filename).getLines()) {
      try {
        if (line startsWith "Date") {

        } else if (line startsWith "-") {

        } else if (line startsWith "Type") {

        } else if (line startsWith "Main") {

        } else {
          series +:= line.toLong
        }
      } catch {
        case _ => throw new Exception("In file " + filename + ": " + line)
      }
    }
    series
  }

  /**
   * Stores a result value series in the field <code>Series</code> in to text file whose name is in <code>Filename</code>
   * with additional information (date and time, main benchmark class name...).
   */
  def store() {

    if (series == Nil) {
      println("Nothing to store")
      return
    }

    filename = null
    println("Input file name to store")

    while (filename == null) {
      filename = Console.readLine()
      try {
        val out = new FileWriter(filename)
        out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm").format(new Date) + "\n"
        out write "Main Class:	" + classname + "\n"
        if (benchmarkType) {
          out write "Type:		Performance\n"
        } else {
          out write "Type:		Memory consumption\n"
        }
        out write "-------------------------------\n"
        for (invidual <- series) {
          out write invidual.toString + "\n"
        }
        out close
      } catch {
        case e => {
          println("There is error in the file name: " + e)
          filename = null
        }
      }
    }
  }

  /**
   * Stores a result value series in the field <code>Series</code> in to text file whose name is the default name
   * in the format: YYYYMMDD.hhmm.BenchmarkClass.BenchmarkType
   * with additional information (date and time, main benchmark class name).
   */
  def storeByDefault() {
    if (series == Nil) {
      println("Nothing to store")
      return
    }

    while (filename == null) {
      if (benchmarkType) {
        filename = new SimpleDateFormat("yyyyMMdd.HHmm.").format(new Date) + classname + ".Performance"
      } else {
        filename = new SimpleDateFormat("yyyyMMdd.HHmm.").format(new Date) + classname + ".MemoryConsumption"
      }

      try {
        val out = new FileWriter(filename)
        out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm").format(new Date) + "\n"
        out write "Main Class:	" + classname + "\n"
        if (benchmarkType) {
          out write "Type:		Performance\n"
        } else {
          out write "Type:		Memory consumption\n"
        }
        out write "-------------------------------\n"
        for (invidual <- series) {
          out write invidual.toString + "\n"
        }
        out close
      } catch {
        case e => {
          filename = null
        }
      }
    }
  }

}