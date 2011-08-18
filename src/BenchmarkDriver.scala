/*
 * BenchmarkDriver
 * 
 * Version 
 * 
 * Created on May 25th 2011
 *
 * Created by ND P
 */

/**
 * Object controls the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {
	/**
	 * Start point of the benchmark driver
	 */
	def main(args: Array[String]): Unit = {

		try {
			var harness = new SteadyHarness(args(0), args(1) + "/", args(3).toInt, args(4).toInt)

//			harness.run

			var reg = new Regression(List())
			reg.regression()

		} catch {
			case e: Exception => throw e
		}
	}

}
