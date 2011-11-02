package scala.tools.sbs
package test
package measurement

import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.UnreliableMeasurementFailure
import scala.tools.sbs.measurement.UnwarmableMeasurementFailure
import scala.util.Random

import org.scalatest.Spec

class BenchmarkRunnerSpec extends Spec {

  describe("BenchmarkRunner") {

    it("should return a UnwarmableFailure when the benchmark cannot be warmed up") {
      val runner = new BenchmarkRunner(testLog)
      assert((runner.run(DummyBenchmark, _ => false, 0)).isInstanceOf[UnwarmableMeasurementFailure])
    }

    it("should return a UnreliableFailure when the benchmark measurement results are not reliable") {
      val runner = new BenchmarkRunner(testLog)
      val result = runner.run(DummyBenchmark, _ => true, Random.nextInt(1000))
      assert(result.isInstanceOf[UnreliableMeasurementFailure])
    }

    it("should return a MeasurementSucces when the benchmark runned fine") {
      val runner = new BenchmarkRunner(testLog)
      val result = runner.run(DummyBenchmark, _ => true, 10)
      assert(result.isInstanceOf[MeasurementSuccess])
      assert(result.asInstanceOf[MeasurementSuccess].series.isReliable)
    }

  }

}
