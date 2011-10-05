package scala.tools.sbs
package test
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.regression.ANOVAFailure
import scala.tools.sbs.regression.ConfidenceIntervalFailure
import scala.tools.sbs.regression.HistoryFactory
import scala.tools.sbs.regression.RegressionSuccess
import scala.tools.sbs.regression.Statistics
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.util.Constant.LEAST_CONFIDENCE_LEVEL

import org.scalatest.Spec

class StatisticsSpec extends Spec {

  private var s: Series = _
  private var testS: Series = _
  private var statistics: Statistics = _

  def init(arr: ArrayBuffer[Long]) {
    s = new Series(testLog, arr, 99)
    statistics = StatisticsFactory(testLog)
  }

  describe("A Statistics") {

    it("should compute sample mean of a given Series") {
      init(ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
      assert((statistics mean s) == 5.5)
    }

    it("should return 0 for a no-element Series") {
      init(ArrayBuffer())
      assert((statistics mean s) == 0)
    }

    it("reduces its confidence level at 100% from the beginning") {
      statistics = StatisticsFactory(testLog)
      assert(statistics.confidenceLevel == 100)
    }

    it("reduces its confidence level from 100% to 99%") {
      statistics = StatisticsFactory(testLog)
      assert(statistics.reduceConfidenceLevel == 99)
    }

    it("reduces its confidence level from 99% to 95%") {
      statistics = StatisticsFactory(testLog, 0.01)
      assert(statistics.reduceConfidenceLevel == 95)
    }

    it("reduces its confidence level from 95% to 90%") {
      statistics = StatisticsFactory(testLog, 0.05)
      assert(statistics.reduceConfidenceLevel == 90)
    }

    it("should not reduce its confidence level in case it has already below threadshold") {
      statistics = StatisticsFactory(testLog, (100 - LEAST_CONFIDENCE_LEVEL + 1) / 100D)
      expect(LEAST_CONFIDENCE_LEVEL - 1)(statistics.reduceConfidenceLevel)
    }

    it("can show whether its confidence level is acceptable") {
      init(null)
      assert(statistics.isConfidenceLevelAcceptable)
      while (statistics.confidenceLevel >= LEAST_CONFIDENCE_LEVEL) {
        assert(statistics.isConfidenceLevelAcceptable)
        statistics.reduceConfidenceLevel
      }
      assert(!statistics.isConfidenceLevelAcceptable)
    }

    it("calculates standard deviation of a Series") {
      init(about1kArr1)
      statistics.reduceConfidenceLevel()
      expect((1043.063850703443, 1055.4816038420115))(statistics confidenceInterval s)
      statistics.reduceConfidenceLevel()
      expect((1044.9076124529304, 1053.6378420925241))(statistics confidenceInterval s)
      statistics.reduceConfidenceLevel()
      expect((1045.7219607209242, 1052.8234938245303))(statistics confidenceInterval s)
    }

    it("yield 0 for standard deviation of a zero-element Series") {
      init(ArrayBuffer())
      expect(0)(statistics standardDeviation s)
    }

    it("calculates coefficient of variant of a Series") {
      init(about5k5Arr)
      expect(0.0078203345273354)(statistics CoV s)
      init(about1kArr1)
      expect(0.006192433880806771)(statistics CoV s)
      init(about1kArr2)
      expect(0.015618439762224599)(statistics CoV s)
      init(about1kArr3)
      expect(0.0070902654423037545)(statistics CoV s)
    }

    it("returns BenchmarkSuccess object if old and new Series are `same same`") {
      statistics = StatisticsFactory(testLog)
      val history = HistoryFactory(testLog, testConfig, DummyBenchmark, SteadyState)
      history add about1kSeries2
      history add about1kSeries1
      var result = statistics.testDifference(DummyBenchmark, SteadyState, success1k1, history)
      expect(RegressionSuccess(DummyBenchmark, SteadyState, 99, success1k1))(result)
    }

    it("returns BenchmarkSuccess object 3 Series are `same same`") {
      statistics = StatisticsFactory(testLog)
      val history = HistoryFactory(testLog, testConfig, DummyBenchmark, SteadyState)
      history add about1kSeries2
      history add about1kSeries3
      history add about1kSeries1
      var result = statistics.testDifference(DummyBenchmark, SteadyState, success1k1, history)
      expect(RegressionSuccess(DummyBenchmark, SteadyState, 99, success1k1))(result)
    }

    it("returns BenchmarkFailure object if 3 Series are statistically significant different") {
      statistics = StatisticsFactory(testLog)
      val history = HistoryFactory(testLog, testConfig, DummyBenchmark, SteadyState)
      history add about1kSeries2
      history add about1kSeries3
      history add about5k5Series
      val result = statistics.testDifference(DummyBenchmark, SteadyState, success1k1, history)
      assert(result.isInstanceOf[ANOVAFailure])
    }

    it("returns BenchmarkFailure object if old and new Series are statistically significant different") {
      statistics = StatisticsFactory(testLog)
      val history = HistoryFactory(testLog, testConfig, DummyBenchmark, SteadyState)
      history add about1kSeries3
      history add about5k5Series
      val result = statistics.testDifference(DummyBenchmark, SteadyState, success1k1, history)
      assert(result.isInstanceOf[ConfidenceIntervalFailure])
    }

    it("raises Exception if history is less than 1") {
      val history = HistoryFactory(testLog, testConfig, DummyBenchmark, SteadyState)
      intercept[Exception] {
        statistics.testDifference(DummyBenchmark, SteadyState, success1k1, history)
      }
    }

  }

}
