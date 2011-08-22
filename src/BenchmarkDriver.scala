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
//			var harness = new MemoryHarness(args(0), args(1) + "/", args(2).toInt, args(3).toInt)

//			for (i <- 1 to 10) harness.run

			var reg = new Regression(Nil)
			reg.run

		} catch {
			case e: Exception => throw e
		}
	}

}
