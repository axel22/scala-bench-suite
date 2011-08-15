/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 11th 2011
 *
 * By ND P
 */

import scala.io.Source.fromFile
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date

class BenchmarkResult {
	
	private var fileName: String = null
	private var series: List[Long] = List()
	
	def this(name: String) {
		this
		fileName = name
	}
	
	def this (timeSeries: List[Long]) {
		this
		series = timeSeries
	}
	
	def this(name: String, timeSeries: List[Long]) {
		this(name)
		series = timeSeries
	}
	
	def load(): List[Long] = {
		for (line <- fromFile(fileName).getLines()) {
			try {
				if (line startsWith "Benchmark") {
					
				}
				else if (line startsWith "-") {
					
				}
				else {
					series ::= line.toLong
				}
			}
			catch {
				case _ => throw new Exception("In file " + fileName + ": " + line)
			}
		}
		series
	}
	
	def store() {
		
		if (this.series == Nil) {
			println("Nothing to store")
			return
		}
		
		this.fileName = null
		println("Input file name to store")
		
		while (this.fileName == null) {
			this.fileName = Console.readLine()
			try {
				val out = new FileWriter(this.fileName)
				out write "Benchmark: " + new SimpleDateFormat().format(new Date) + "\n"
				out write "-------------------------------\n"
				for (invidual <- series) {
					out write invidual.toString + "\n"
				}
				out close
			}
			catch {
				case e => {
					println("There is error in the file name: " + e)
					this.fileName = null
				}
			}
		}
	}
	
	def TimeSeries = series

}