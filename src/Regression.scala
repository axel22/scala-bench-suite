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
import scala.math.pow
import scala.math.abs

class Regression(SERIES: List[List[Long]]) {

	var statistic: Statistic = null
	val alpha = 0.05

	def compare() {

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
				println(" At confidence level 1 - alpha " + " no statistic significant difference")
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

			if (SSA * (SERIES.length * SERIES.head.length - SERIES.length) / SSE / (SERIES.length - 1) > F(SERIES.length - 1, SERIES.length * SERIES.head.length - SERIES.length)) {

			} else {

			}
		}
	}

	def F(df1: Int, df2: Int): Double = {
		var result: Double = interpolate(1, df1, df2)
		var temp = interpolate(result, df1, df2)
		while (abs(result - temp) < 0.00001) {
			temp = result
			result = interpolate(result, df1, df2)
		}
		result
	}

	def interpolate(x: Double, df1: Double, df2: Double): Double = {
		pow(((1 - alpha) * betaIntegral(df1 / 2, df2 / 2) * pow(df2, df1 / 2) * pow(df1 * x + df2, (df1 + df2) / 2)) / (pow(df1, df1 / 2) * pow(df2, (df1 + df2) / 2)), 2 / (df1 - 2))
	}

	def betaIntegral(x: Double, y: Double): Double = {
		var t: Double = 0
		var result: Double = 0
		while (t <= 1) {
			result += pow(t, x - 1) * pow(1 - t, y - 1)
			t += 0.05
		}
		result
	}
}