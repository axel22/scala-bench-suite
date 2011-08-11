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

class StoredResult {
	
	private var fileName: String = null
	private var series: List[Long] = List()
	
	def this(name:String) {
		this
		fileName = name
	}
	
	def load() {
		for (line <- fromFile(fileName).getLines()) {
			try {
				series ::= line.toLong
			}
			catch {
				case _ => println(line)
			}
		}
	}
	
	def TimeSeries = series

}