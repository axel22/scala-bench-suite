package scala.tools.sbs
package test
package performance

import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.performance.SeriesAchiever
import scala.tools.sbs.performance.UnreliableMeasurementFailure
import scala.tools.sbs.performance.UnwarmableMeasurementFailure
import scala.util.Random

import org.scalatest.Spec

class SeriesAchieverSpec extends Spec {

  describe("BenchmarkRunner") {

    it("should return a UnwarmableFailure when the benchmark cannot be warmed up") {
      val seriesAchiever = new SeriesAchiever(testConfig, testLog)
      assert((seriesAchiever.achieve(DummyBenchmark, _ => false, () => 0)).isInstanceOf[UnwarmableMeasurementFailure])
    }

    it("should return a UnreliableFailure when the benchmark measurement results are not reliable") {
      val seriesAchiever = new SeriesAchiever(testConfig, testLog)
      val result = seriesAchiever.achieve(DummyBenchmark, _ => true, () => Random.nextInt(1000))
      assert(result.isInstanceOf[UnreliableMeasurementFailure])
    }

    it("should return a MeasurementSucces when the benchmark runned fine") {
      val seriesAchiever = new SeriesAchiever(testConfig, testLog)
      val result = seriesAchiever.achieve(DummyBenchmark, _ => true, () => 10)
      assert(result.isInstanceOf[MeasurementSuccess])
      assert(result.asInstanceOf[MeasurementSuccess].series.isReliable)
    }

  }

}
