package scala.tools.sbs
package test
package measurement

import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.benchmark.BenchmarkMode
import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.UnreliableFailure
import scala.tools.sbs.measurement.UnwarmableFailure
import scala.util.Random

import org.scalatest.Spec

class BenchmarkRunnerSpec extends Spec {

  object DummyBenchmark extends Benchmark {
    def name() = "AnDummyBenchmark"
    def modes() = List(BenchmarkMode.STEADY)
    def compile() = true
    def init() = ()
    def run() = ()
    def finallize() = ()
    def initCommand() = true
    def runCommand() = ()
    override def toString() = name
  }

  describe("BenchmarkRunner") {

    it("should return a UnwarmableFailure when the benchmark cannot be warmed up") {
      val runner = new BenchmarkRunner(testLog, testConfig)
      assert((runner.run(DummyBenchmark, _ => false, 0)).isInstanceOf[UnwarmableFailure])
    }

    it("should return a UnreliableFailure when the benchmark cannot be warmed up") {
      val runner = new BenchmarkRunner(testLog, testConfig)
      val result = runner.run(DummyBenchmark, _ => true, Random.nextInt(1000))
      assert(result.isInstanceOf[UnreliableFailure])
      assert(!result.series.isReliable)
    }

    it("should return a MeasurementSucces when the benchmark runned fine") {
      val runner = new BenchmarkRunner(testLog, testConfig)
      val result = runner.run(DummyBenchmark, _ => true, 10)
      assert(result.series.isReliable)
      assert(result.isInstanceOf[MeasurementSuccess])
    }

  }

}
