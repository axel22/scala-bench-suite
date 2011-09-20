package scala.tools.sbs
package test
package regression

import org.scalatest.Spec
import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.measurement.Series
import scala.tools.sbs.measurement.ArrayBufferSeries
import scala.tools.sbs.regression.StatisticFactory
import scala.tools.sbs.regression.Statistic

class SimpleStatisticSpec extends Spec {

  private var s: Series = _
  private var statistic: Statistic = _

  def init(arr: ArrayBuffer[Long]) {
    s = new ArrayBufferSeries(testLog, testConfig, arr, 99)
    statistic = new StatisticFactory(testLog, testConfig).create()
  }

  describe("A Statistic") {

    it("should compute sample mean of a given Series") {
      init(ArrayBuffer(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
      assert((statistic mean s) == 5.5)
      s.clear()
      assert((statistic mean s) == 0)
    }

    it("reduces its confidence level from 100% to 99%, 95%, 90%") {
      init(null)
      assert(statistic.confidenceLevel == 100)
      assert(statistic.reduceConfidenceLevel == 99)
      assert(statistic.reduceConfidenceLevel == 95)
      assert(statistic.reduceConfidenceLevel == 90)
      assert(statistic.reduceConfidenceLevel == 85)
      assert(statistic.reduceConfidenceLevel == 85)
    }

    it("reduces its confidence level until it can be acceptable (90%)") {
      init(null)
      assert(statistic.confidenceLevel == 100)
      assert(statistic.isConfidenceLevelAcceptable)
      assert(statistic.reduceConfidenceLevel == 99)
      assert(statistic.isConfidenceLevelAcceptable)
      assert(statistic.reduceConfidenceLevel == 95)
      assert(statistic.isConfidenceLevelAcceptable)
      assert(statistic.reduceConfidenceLevel == 90)
      assert(statistic.isConfidenceLevelAcceptable)
      assert(statistic.reduceConfidenceLevel == 85)
      assert(!statistic.isConfidenceLevelAcceptable)
    }

    it("calculates standard deviation of a Series") {
      init(about1kSeries1)
      statistic.reduceConfidenceLevel()
      expect((1043.063850703443, 1055.4816038420115))(statistic confidenceInterval s)
      statistic.reduceConfidenceLevel()
      expect((1044.9076124529304, 1053.6378420925241))(statistic confidenceInterval s)
      statistic.reduceConfidenceLevel()
      expect((1045.7219607209242, 1052.8234938245303))(statistic confidenceInterval s)
    }
    
    it("calculates coefficient of variant of a Series") {
      init(about5k5Series)
      expect(0.0078203345273354)(statistic CoV s)
      init(about1kSeries1)
      expect(0.006192433880806771)(statistic CoV s)
      init(about1kSeries2)
      expect(0.015618439762224599)(statistic CoV s)
      init(about1kSeries3)
      expect(0.0070902654423037545)(statistic CoV s)
    }

  }

}
