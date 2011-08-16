/*
 * BenchmarkResult
 * 
 * Version 
 * 
 * Created on August 11th 2011
 *
 * Created by ND P
 */

import scala.io.Source.fromFile
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date


/**
 * Class represents the result of benchmarking. Allow user to store or load a list of running time from file.
 *
 * @author ND P
 */
class BenchmarkResult {
	
	/**
	 * The name of the file contains the benchmarking result
	 */
	private var filename: String = null
	/**
	 * The <code>List[Long]> represent the result running time series
	 */
	private var series: List[Long] = List()
	/**
	 * The name of the benchmark class
	 */
	private var classname: String = null
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given file name.
	 * 
	 * @param fileName	the name of the file used for loading or storing.
	 */
	def this(fileName: String) {
		this
		filename = fileName
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given running time series.
	 * 
	 * @param timeSeries	the result time series of a benchmarking to be stored.
	 */
	def this (timeSeries: List[Long]) {
		this
		series = timeSeries
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given class name and running time series.
	 * 
	 * @param timeSeries	the result time series of a benchmarking to be stored.
	 * @param className	the name of the benchmark class.
	 */
	def this (timeSeries: List[Long], className: String) {
		this (timeSeries)
		classname = className 
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given file name and running time series.
	 * 
	 * @param fileName	the name of the file used for storing.
	 * @param timeSeries	the result time series of a benchmarking to be stored.
	 */
	def this(fileName: String, timeSeries: List[Long]) {
		this(fileName)
		series = timeSeries
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given file name, running time series and class name.
	 * 
	 * @param fileName	the name of the file used for storing.
	 * @param timeSeries	the result time series of a benchmarking to be stored.
	 * @param className	the name of the benchmark class.
	 */
	def this(fileName: String, timeSeries: List[Long], className: String) {
		this(timeSeries, className)
		classname = fileName
		
	}
	
	/**
	 * Loads a running time series from file whose name is in the field <code>filename</code>.
	 * @return	the result time series
	 */
	def load(): List[Long] = {
		for (line <- fromFile(filename).getLines()) {
			try {
				if (line startsWith "Benchmark") {
					
				}
				else if (line startsWith "-") {
					
				}
				else if (line startsWith "Main") {
					
				}
				else {
					series ::= line.toLong
				}
			}
			catch {
				case _ => throw new Exception("In file " + filename + ": " + line)
			}
		}
		series
	}
	
	/**
	 * Stores a running time series in the field <code>series</code> in to text file whose name is in <code>filename</code> 
	 * with additional information (date and time, main benchmark class name).
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
				out write "Benchmark: " + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm").format(new Date) + "\n"
				out write "Main Class: " + classname + "\n"
				out write "-------------------------------\n"
				for (invidual <- series) {
					out write invidual.toString + "\n"
				}
				out close
			}
			catch {
				case e => {
					println("There is error in the file name: " + e)
					filename = null
				}
			}
		}
	}
	
	/**
	 * @return	The time series property
	 */
	def TimeSeries = series

}