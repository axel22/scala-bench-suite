/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on August 09th 2011
 *
 * By ND P
 */

import scala.math.Pi
import scala.math.atan
import scala.math.cos
import scala.math.pow
import scala.math.sin
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
		var storedResult: StoredResult = null
		var line: String = null
		
		println("Input previous result file, double-enter to stop")
		do {
			line = Console.readLine()
			if (!line.equals("")) {
				try {
					storedResult = new StoredResult(line)
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
				c1 = mean - statistic.getGaussian(alpha) * s
				c2 = mean + statistic.getGaussian(alpha) * s
			} else {
				val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
				c1 = mean - statistic.getStudent(alpha, ndf) * s
				c2 = mean + statistic.getStudent(alpha, ndf) * s
			}

			if (((c1 > 0) && (c2 > 0)) || ((c1 < 0) && (c2 < 0))) {
				println()
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

			if (SSA * (SERIES.length * SERIES.head.length - SERIES.length) / SSE / (SERIES.length - 1) > FInverseCDF(alpha, SERIES.length - 1, SERIES.length * SERIES.head.length - SERIES.length)) {
				println(" At confidence level " + (1 - alpha) + " no statistic significant difference")
			}
			else {
				println()
			}
		}
	}

	def FCDF(f: Double, n1: Double, n2: Double): Double = {
		var x: Double = n2 / (n1 * f + n2)
		if ((n1 % 2) == 0) {
			return calculateStat(1 - x, n2.asInstanceOf[Int], (n1 + n2).asInstanceOf[Int] - 4, n2.asInstanceOf[Int] - 2) * pow(x, n2 / 2.0)
		}
		if ((n2 % 2) == 0) {
			return 1 - calculateStat(x, n1.asInstanceOf[Int], (n1 + n2).asInstanceOf[Int] - 4, n1.asInstanceOf[Int] - 2) * pow(1 - x, n1 / 2.0)
		}
		val th: Double = atan(sqrt(n1 * f / n2))
		var a: Double = th / Pi
		val sth: Double = sin(th)
		val cth: Double = cos(th)
		if (n2 > 1) {
			a = a + sth * cth * calculateStat(cth * cth, 2, n2.asInstanceOf[Int] - 3, -1) / Pi * 2
		}
		if (n1 == 1) {
			return 1 - a
		}
		var c: Double = 4 * calculateStat(sth * sth, n2.asInstanceOf[Int] + 1, (n1 + n2).asInstanceOf[Int] - 4, n2.asInstanceOf[Int] - 2) * sth * pow(cth, n2) / Pi
		if (n2 == 1) {
			return 1 - a + c / 2
		}
		var k: Double = 2
		while (k <= (n2 - 1) / 2) {
			c = c * k / (k - 0.5)
			k = k + 1
		}
		1 - a + c
	}

	def calculateStat(q: Double, i: Int, j: Int, b: Int): Double = {
		var zz: Double = 1
		var z: Double = zz
		var k: Double = i
		while (k <= j) {
			zz = zz * q * k / (k - b)
			z = z + zz
			k = k + 2
		}
		z
	}

	def FInverseCDF(p: Double, n1: Double, n2: Double): Double = {
		var v: Double = 0.5
		var dv: Double = 0.5
		var f: Double = 0
		while (dv > 1e-10) {
			f = 1 / v - 1
			dv = dv / 2
			if (FCDF(f, n1, n2) > 1 - p) {
				v = v - dv
			}
			else {
				v = v + dv
			}
		}
		f
	}
}