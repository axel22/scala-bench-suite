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

			harness.run

			/*for (i <- harness.RunningTimes) {
				println("[Running Time] 	" + i + "ms")
			}
			println("[Sample Mean]	" + harness.SampleMean.formatted("%.2f") + "ms")
			println("[Confident Interval]	[" + harness.ConfidentIntervalLeft.formatted("%.2f") + "; " + harness.ConfidentIntervalRight.formatted("%.2f") + "]")
			var diff = (harness.ConfidentIntervalRight - harness.ConfidentIntervalLeft) / 2
			println("[Difference] " + diff.formatted("%.2f") + "ms = " + (diff / harness.SampleMean * 100).formatted("%.2f") + "%")*/

		} catch {
			case e: Exception => throw e
		}
	}
}
