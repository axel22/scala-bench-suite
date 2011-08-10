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
			// TODO
		}
	}
}