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
	
	private var FileName: String = null
	private var TimeSeries: List[Long] = List()
	
	def this(fileName:String) {
		this
		FileName = fileName
	}
	
	def load() {
		val file = fromFile(FileName)
		var lines = file.getLines()
		for (line <- lines) {
			if (line.startsWith("a")) {
				println(line)
			}
		}
	}

}