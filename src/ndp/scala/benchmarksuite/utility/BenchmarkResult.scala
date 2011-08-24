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


/**
 * Class represents the result of benchmarking. Allow user to store or load a list of running time from file.
 *
 * @author ND P
 */
class BenchmarkResult {
	
	/**
	 * The name of the file contains the benchmarking result.
	 */
	private var Filename: String = null
	/**
	 * The <code>List[Long]</code> represent the result running time series.
	 */
	private var Series: List[Long] = List()
	/**
	 * The name of the benchmark class
	 */
	private var Classname: String = null
	/**
	 * The type of benchmarking (performance or memory consumption).
	 * <ul>
	 * <li><code>true</code> value for performance benchmarking
	 * <li><code>false</code> value for memory consumption benchmarking
	 * </ul>
	 */
	private var BenchmarkType: Boolean = true
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given file name.
	 * 
	 * @param theFilename	The name of the file used for loading or storing.
	 */
	def this(theFilename: String) {
		this
		Filename = theFilename
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given value series.
	 * 
	 * @param theSeries	The result value series of a benchmarking to be stored.
	 */
	def this (theSeries: List[Long]) {
		this
		Series = theSeries
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given class name and value series.
	 * 
	 * @param theSeries	The result value series of a benchmarking to be stored.
	 * @param theClassname	The name of the benchmark class.
	 */
	def this (theSeries: List[Long], theClassname: String) {
		this (theSeries)
		Classname = theClassname 
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given class name, value series and benchmarking type.
	 * 
	 * @param theSeries	The result value series of a benchmarking to be stored.
	 * @param theClassname	The name of the benchmark class.
	 * @param theBenchmarkType	The type of benchmarking.	
	 */
	def this(theSeries: List[Long], theClassname: String, theBenchmarkType: Boolean) {
		this(theSeries, theClassname)
		BenchmarkType = theBenchmarkType
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given file name and value series.
	 * 
	 * @param theFilename	The name of the file used for storing.
	 * @param theSeries	The result value series of a benchmarking to be stored.
	 */
	def this(theFilename: String, theSeries: List[Long]) {
		this(theFilename)
		Series = theSeries
	}
	
	/**
	 * Constructs a <code>BenchmarkResult</code> using the given informations.
	 * 
	 * @param theFilename	The name of the file used for storing.
	 * @param theSeries	The result value series of a benchmarking to be stored.
	 * @param theClassname	The name of the benchmark class.
	 */
	def this(theFilename: String, theSeries: List[Long], theClassname: String, theBenchmarkType: Boolean) {
		this(theSeries, theClassname)
		Filename = theFilename
		BenchmarkType = theBenchmarkType
	}
	
	/**
	 * Loads a result value series from file whose name is in the field <code>Filename</code>.
	 * @return	The result value series
	 */
	def load(): List[Long] = {
		for (line <- fromFile(Filename).getLines()) {
			try {
				if (line startsWith "Date") {
					
				}
				else if (line startsWith "-") {
					
				}
				else if (line startsWith "Type") {
					
				}
				else if (line startsWith "Main") {
					
				}
				else {
					Series ::= line.toLong
				}
			}
			catch {
				case _ => throw new Exception("In file " + Filename + ": " + line)
			}
		}
		Series
	}
	
	/**
	 * Stores a result value series in the field <code>Series</code> in to text file whose name is in <code>Filename</code> 
	 * with additional information (date and time, main benchmark class name...).
	 */
	def store() {
		
		if (Series == Nil) {
			println("Nothing to store")
			return
		}
		
		Filename = null
		println("Input file name to store")
		
		while (Filename == null) {
			Filename = Console.readLine()
			try {
				val out = new FileWriter(Filename)
				out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm").format(new Date) + "\n"
				out write "Main Class:	" + Classname + "\n"
				if (BenchmarkType) {
					out write "Type:		Performance\n"
				}
				else {
					out write "Type:		Memory consumption\n"
				}
				out write "-------------------------------\n"
				for (invidual <- Series) {
					out write invidual.toString + "\n"
				}
				out close
			}
			catch {
				case e => {
					println("There is error in the file name: " + e)
					Filename = null
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
		if (Series == Nil) {
			println("Nothing to store")
			return
		}
		
		while (Filename == null) {
			if (BenchmarkType) {
				Filename = new SimpleDateFormat("yyyyMMdd.HHmm.").format(new Date) + Classname + ".Performance" 
			}
			else {
				Filename = new SimpleDateFormat("yyyyMMdd.HHmm.").format(new Date) + Classname + ".MemoryConsumption" 
			}
			
			try {
				val out = new FileWriter(Filename)
				out write "Date:		" + new SimpleDateFormat("yyyy/MM/dd 'at' HH:mm").format(new Date) + "\n"
				out write "Main Class:	" + Classname + "\n"
				if (BenchmarkType) {
					out write "Type:		Performance\n"
				}
				else {
					out write "Type:		Memory consumption\n"
				}
				out write "-------------------------------\n"
				for (invidual <- Series) {
					out write invidual.toString + "\n"
				}
				out close
			}
			catch {
				case e => {
					Filename = null
				}
			}
		}
	}
	
	/**
	 * @return	The time series property
	 */
	def getSeries = Series

}