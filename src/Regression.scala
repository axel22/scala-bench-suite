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
	private var LIST: List[List[Long]] = List()
	/**
	 * The <code>Statistic</code> class used for computing statistic arguments.
	 */
	private var statistic: Statistic = null

	/**
	 * Constructs a <code>Regression</code> using the given the list of value series.
	 * 
	 * @param series	the list of running time series.
	 */
	def this(theList: List[List[Long]]) {
		this
		this.LIST = theList
	}
	
	/**
	 * Loads results files and invokes the <code>run</code> function.
	 */
	def run() {
		var storedResult: BenchmarkResult = null
		var line: String = null
		
		println("Input previous result file, double-enter to stop")
		do {
			line = Console.readLine()
			if (!line.equals("")) {
				try {
					storedResult = new BenchmarkResult(line)
					storedResult.load()
					LIST ::= storedResult.getSeries
				}
				catch {
					case _ => println("File name incorrect")
				}
			}
		}
		while (!line.equals(""))
			
		statistic = new Statistic(Nil)
		statistic.setLIST(LIST)
		
		if (statistic.testDifference) {
			println("At confidence level " + statistic.ConfidentLevel + "% there is statistically significant difference")
		} else {
			println("At confidence level " + statistic.ConfidentLevel + "% no statistically significant difference")
		}
	}

}