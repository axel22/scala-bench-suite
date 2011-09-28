package scala.tools.sbs
package test
package measurement

import scala.tools.sbs.measurement.BenchmarkRunner
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.measurement.UnreliableFailure
import scala.tools.sbs.measurement.UnwarmableFailure
import scala.util.Random

import org.scalatest.Spec

class BenchmarkRunnerSpec extends Spec {

  object DummyBenchmark extends Benchmark {
    def src = testDir
    def arguments = List[String]()
    def classpathURLs = testConfig.classpathURLs
    def runs = 1
    def multiplier = 10
    def sampleNumber = 0
    def shouldCompile = false
    def log = testLog
    def init() = ()
    def run() = ()
    def reset() = ()
    def initCommand() = true
    def runCommand() = ()
    def toXML: scala.xml.Elem = <null/>
  }

  describe("BenchmarkRunner") {

    it("should return a UnwarmableFailure when the benchmark cannot be warmed up") {
      val runner = new BenchmarkRunner(testLog)
      assert((runner.run(DummyBenchmark, _ => false, 0)).isInstanceOf[UnwarmableFailure])
    }

    it("should return a UnreliableFailure when the benchmark measurement results are not reliable") {
      val runner = new BenchmarkRunner(testLog)
      val result = runner.run(DummyBenchmark, _ => true, Random.nextInt(1000))
      assert(result.isInstanceOf[UnreliableFailure])
    }

    it("should return a MeasurementSucces when the benchmark runned fine") {
      val runner = new BenchmarkRunner(testLog)
      val result = runner.run(DummyBenchmark, _ => true, 10)
      assert(result.isInstanceOf[MeasurementSuccess])
      assert(result.asInstanceOf[MeasurementSuccess].series.isReliable)
    }

  }

}
