package scala.tools.sbs
package test
package regression

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.measurement.Series
import org.scalatest.Spec
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.regression.Statistics

class SimpleStatisticsSpec extends Spec {

  private var s: Series = _
  private var statistics: Statistics = _

  def init(arr: ArrayBuffer[Long]) {
    s = new Series(testLog, arr, 99)
    statistics = StatisticsFactory(testLog)
  }

  describe("A Statistic") {

    it("should compute sample mean of a given Series") {
      init(ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
      assert((statistics mean s) == 5.5)
      s.clear()
      assert((statistics mean s) == 0)
    }

    it("reduces its confidence level from 100% to 99%, 95%, 90%") {
      init(null)
      assert(statistics.confidenceLevel == 100)
      assert(statistics.reduceConfidenceLevel == 99)
      assert(statistics.reduceConfidenceLevel == 95)
      assert(statistics.reduceConfidenceLevel == 90)
      assert(statistics.reduceConfidenceLevel == 85)
      assert(statistics.reduceConfidenceLevel == 85)
    }

    it("reduces its confidence level until it can be acceptable (90%)") {
      init(null)
      assert(statistics.confidenceLevel == 100)
      assert(statistics.isConfidenceLevelAcceptable)
      assert(statistics.reduceConfidenceLevel == 99)
      assert(statistics.isConfidenceLevelAcceptable)
      assert(statistics.reduceConfidenceLevel == 95)
      assert(statistics.isConfidenceLevelAcceptable)
      assert(statistics.reduceConfidenceLevel == 90)
      assert(statistics.isConfidenceLevelAcceptable)
      assert(statistics.reduceConfidenceLevel == 85)
      assert(!statistics.isConfidenceLevelAcceptable)
    }

    it("calculates standard deviation of a Series") {
      init(about1kSeries1)
      statistics.reduceConfidenceLevel()
      expect((1043.063850703443, 1055.4816038420115))(statistics confidenceInterval s)
      statistics.reduceConfidenceLevel()
      expect((1044.9076124529304, 1053.6378420925241))(statistics confidenceInterval s)
      statistics.reduceConfidenceLevel()
      expect((1045.7219607209242, 1052.8234938245303))(statistics confidenceInterval s)
    }
    
    it("calculates coefficient of variant of a Series") {
      init(about5k5Series)
      expect(0.0078203345273354)(statistics CoV s)
      init(about1kSeries1)
      expect(0.006192433880806771)(statistics CoV s)
      init(about1kSeries2)
      expect(0.015618439762224599)(statistics CoV s)
      init(about1kSeries3)
      expect(0.0070902654423037545)(statistics CoV s)
    }

  }

}
