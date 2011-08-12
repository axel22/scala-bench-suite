/**
 * Scala Benchmark Suite
 *
 * Copyright 2011 HCMUT - EPFL
 *
 * Created on May 25th 2011
 *
 * By ND P
 */


/**
 * Control the runtime of benchmark classes to do measurements.
 *
 * @author ND P
 */
object BenchmarkDriver {
	/**
	 * Start point of the benchmark driver
	 */
	def main(args: Array[String]): Unit = {

		try {
			var harness = new SteadyHarness(args(0), args(1) + "\\", args(2).toInt, args(3).toInt, args(4).toInt)

			//harness.run
			
			var reg = new Regression(List())
			
			println(reg.FInverseCDF(0.99, 1, 2))
			
			val a = new BenchmarkResult("D:\\out", List(1, 34, 3243, 798)).load()
			println(a)
			
		} catch {
			case e: Exception => throw e
		}
	}

}
