/*
 * TwinningMeasurer
 * 
 * Version
 * 
 * Created on November 1st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint
package strategy

import scala.collection.mutable.ArrayBuffer
import scala.tools.nsc.io.Directory
import scala.tools.sbs.common.Backuper
import scala.tools.sbs.common.Reflector
import scala.tools.sbs.io.Log
import scala.tools.sbs.performance.regression.HistoryFactory
import scala.tools.sbs.performance.regression.RegressionFailure
import scala.tools.sbs.performance.regression.RegressionResult
import scala.tools.sbs.performance.regression.RegressionSuccess
import scala.tools.sbs.performance.regression.StatisticsFactory
import scala.tools.sbs.performance.MeasurementFailure
import scala.tools.sbs.performance.MeasurementResult
import scala.tools.sbs.performance.MeasurementSuccess
import scala.tools.sbs.performance.PerformanceBenchmark
import scala.tools.sbs.util.Constant

trait TwinningDetector {
  self: Configured =>

  def twinningDetect[Detected](benchmark: PerformanceBenchmark,
                               measureCurrent: => MeasurementResult,
                               measurePrevious: => MeasurementResult,
                               regressionSuccess: RegressionSuccess => Detected,
                               regressionFailure: RegressionFailure => Detected,
                               measurementFailure: MeasurementFailure => Detected): Detected = {
    def onMeasurementFailure(failure: MeasurementFailure) = {
      log.error("  Measuring current performance failed due to: " + failure.reason)

      log.info("[    Run FAILED    ]" + Constant.ENDL)

      measurementFailure(failure)
    }

    log.info("  Measure current performance")
    val current = measureCurrent

    log.debug("    Current:  " + current.getClass.getName)

    current match {
      case currentSuccess: MeasurementSuccess => {

        log.info("  Measure previous performance")

        val previous = measurePrevious

        log.debug("    Previous: " + previous.getClass.getName)

        previous match {
          case previousSuccess: MeasurementSuccess => {

            log.info("[      Run OK      ]")

            regress(benchmark, currentSuccess, previousSuccess) match {
              case regressSuccess: RegressionSuccess => {

                log.info("[  Performance OK  ]" + Constant.ENDL)

                regressionSuccess(regressSuccess)
              }
              case regressFailure: RegressionFailure => {

                log.info("[Performance FAILED]" + Constant.ENDL)

                regressionFailure(regressFailure)
              }
            }
          }
          case failure: MeasurementFailure => onMeasurementFailure(failure)
        }
      }
      case failure: MeasurementFailure => onMeasurementFailure(failure)
    }
  }

  protected def regress(benchmark: PerformanceBenchmark,
                        current: MeasurementSuccess,
                        previous: MeasurementSuccess): RegressionResult = {
    val history = HistoryFactory(benchmark, Pinpointing)
    history add previous.series
    StatisticsFactory(config, log) testDifference (benchmark, current.series, history)
  }

}
