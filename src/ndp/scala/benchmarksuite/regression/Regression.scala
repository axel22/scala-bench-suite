/*
 * Regression
 * 
 * Version 
 * 
 * Created on August 9th 2011
 *
 * Created by ND P
 */

package ndp.scala.benchmarksuite.regression

import ndp.scala.benchmarksuite.utility.BenchmarkResult

/**
 * Class uses previous benchmarking results to detect regression.
 * 
 * @author ND P
 */
class Regression() {

	/**
	 * List of the value series.
	 */
	private var LIST: List[List[Long]] = Nil
	/**
	 * The <code>Statistic</code> class used for computing statistic metrics.
	 */
	private var statistic: Statistic = null

	/**
	 * Constructs a <code>Regression</code> using the given the list of value series.
	 * 
	 * @param series	The list of value series.
	 */
	def this(theList: List[List[Long]]) {
		this
		this.LIST = theList
	}
	
	/**
	 * Loads benchmark histories from files and uses <code>Statistic</code> class to detect regression.
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
		
		try {
			if (statistic.testDifference) {
				println("At confidence level " + statistic.ConfidentLevel + "% there is statistically significant difference")
			} else {
				println("At confidence level " + statistic.ConfidentLevel + "% no statistically significant difference")
			}
		}
		catch {
			case e => println(e.getMessage())
		}
	}

}