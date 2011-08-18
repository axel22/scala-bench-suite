/*
 * Regression
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

import scala.math.sqrt


/**
 * Class uses previous benchmarking results to detect regression.
 * 
 * @author ND P
 */
class Regression() {

	/**
	 * List of the running time series.
	 */
	private var SERIES: List[List[Long]] = List()
	/**
	 * The <code>Statistic</code> class used for computing statistic arguments.
	 */
	private var statistic: Statistic = null

	/**
	 * Constructs a <code>Regression</code> using the given the list of running time series.
	 * 
	 * @param series	the list of running time series.
	 */
	def this(series: List[List[Long]]) {
		this
		this.SERIES = series
	}
	
	/**
	 * Loads results files and invokes the <code>run</code> function.
	 */
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
					SERIES ::= storedResult.getSeries
				}
				catch {
					case _ => println("File name incorrect")
				}
			}
		}
		while (!line.equals(""))
			
		run()
	}
	
	/**
	 * Detects regression from a list of running time series using statistically rigorous evaluation method:
	 * <ul>
	 * <li>Confidence intervals for comparing 2 alternatives.
	 * <li>ANOVA for comparing 3 or more alternatives.
	 * </ul>
	 */
	def run() {

		if (SERIES.length < 2) {
			println("No gression")
			return
		}
		if (SERIES.length > 11) {
			println("Not support such many alternatives, maximum is 11")
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
				c1 = mean - statistic.inverseGaussianDistribution * s
				c2 = mean + statistic.inverseGaussianDistribution * s
			} else {
				val ndf: Int = ((s1 * s1 / n1 + s2 * s2 / n2) * (s1 * s1 / n1 + s2 * s2 / n2) / ((s1 * s1 / n1) * (s1 * s1 / n1) / (n1 - 1) + (s2 * s2 / n2) * (s2 * s2 / n2) / (n2 - 1))).toInt
				c1 = mean - statistic.inverseStudentDistribution(ndf) * s
				c2 = mean + statistic.inverseStudentDistribution(ndf) * s
			}
			
			println("[Mean] " + mean + "\t[Standard Deviation] " + s)
			println("[Confidence Interval] [" + c1 + "; " + c2 + "]")

			if (((c1 > 0) && (c2 > 0)) || ((c1 < 0) && (c2 < 0))) {
				println("At confidence level " + statistic.ConfidentLevel + "% there is statistic significant difference")
			} else {
				println("At confidence level " + statistic.ConfidentLevel + "% no statistic significant difference")
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
			
			val n1 = SERIES.length - 1
			val n2 = SERIES.length * SERIES.head.length - SERIES.length
			val FValue: Double = SSA * n2 / SSE / n1
			println("[SSA] " + SSA + "\t[SSE] " + SSE + "\t[FValue] " + FValue + "\t[F(" + n1 + ", " + n2 + ")] " + statistic.inverseFDistribution(n1, n2))
			
			if (FValue > statistic.inverseFDistribution(n1, n2)) {
				println("At confidence level " + statistic.ConfidentLevel + "% there is statistic significant difference")
			}
			else {
				println("At confidence level " + statistic.ConfidentLevel + "% no statistic significant difference")
			}
		}
	}

}