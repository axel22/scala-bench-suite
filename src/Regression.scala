/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 09th 2011
 *
 * By ND P
 */

import scala.math.sqrt

class Regression() {

	private var SERIES: List[List[Long]] = List()
	private var statistic: Statistic = null
	val alpha = 0.05

	def this(series: List[List[Long]]) {
		this
		this.SERIES = series
	}
		
	def regression() {
		var storedResult: BenchmarkResult = null
		var line: String = null
		
		println("Input previous result file, double-enter to stop")
		do {
			line = Console.readLine()
			if (!line.equals("")) {
				try {
					storedResult = new BenchmarkResult(line)
					storedResult.load()
					SERIES ::= storedResult.TimeSeries
				}
				catch {
					case _ => println("File name incorrect")
				}
			}
		}
		while (!line.equals(""))
			
		println(SERIES)
		
		run()
	}
	
	def run() {

		if (SERIES.length < 2) {
			println("No gression")
			return
		}
		if (SERIES.length == 2) {
			statistic = new Statistic(SERIES.head)

			val mean1 = statistic.Mean
			val s1 = statistic.StandardDeviation
			val n1 = SERIES.head.length

			statistic = new Statistic(SERIES.last)

			val mean2 = statistic.Mean
			val s2 = statistic.StandardDeviation
			val n2 = SERIES.last.length

			val mean = mean1 - mean2
			val s = sqrt(s1 * s1 / n1 + s2 * s2 / n2)

			var c1: Double = 0
			var c2: Double = 0

			if ((n1 >= 30) && (n2 >= 30)) {
				c1 = mean - statistic.inverseGaussianDistribution(alpha) * s
				c2 = mean + statistic.inverseGaussianDistribution(alpha) * s
			} else {
				val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
				c1 = mean - statistic.inverseStudentDistribution(alpha, ndf) * s
				c2 = mean + statistic.inverseStudentDistribution(alpha, ndf) * s
			}

			if (((c1 > 0) && (c2 > 0)) || ((c1 < 0) && (c2 < 0))) {
				println(" At confidence level " + (1 - alpha) + " there is statistic significant difference")
			} else {
				println(" At confidence level " + (1 - alpha) + " no statistic significant difference")
			}
		} else {
			var sum: Long = 0
			for (alternative <- SERIES) {
				for (invidual <- alternative) {
					sum += invidual
				}
			}
			val overall = sum / (SERIES.length * SERIES.head.length)

			var SSA: Double = 0
			var SSE: Double = 0
			for (alternative <- SERIES) {
				statistic = new Statistic(alternative)
				val alternativeMean = statistic.Mean
				SSA += (alternativeMean - overall) * (alternativeMean - overall)

				for (invidual <- alternative) {
					SSE += (invidual - alternativeMean) * (invidual - alternativeMean)
				}
			}
			SSA *= SERIES.head.length

			val FValue: Double = SSA * (SERIES.length * SERIES.head.length - SERIES.length) / SSE / (SERIES.length - 1)
			
			if (FValue > statistic.inverseFDistribution(alpha, SERIES.length - 1, SERIES.length * SERIES.head.length - SERIES.length)) {
				println(" At confidence level " + (1 - alpha) + " no statistic significant difference")
			}
			else {
				println(" At confidence level " + (1 - alpha) + " there is statistic significant difference")
			}
		}
	}

}