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
				case _ => println(line)
			}
		}
		series
	}
	
	def store() {
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
			case e => throw e
		}
	}
	
	def TimeSeries = series

}