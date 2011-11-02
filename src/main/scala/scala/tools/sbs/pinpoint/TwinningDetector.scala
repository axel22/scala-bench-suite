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

import scala.tools.sbs.io.Log
import scala.tools.sbs.io.UI
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.regression.HistoryFactory
import scala.tools.sbs.regression.RegressionFailure
import scala.tools.sbs.regression.RegressionResult
import scala.tools.sbs.regression.RegressionSuccess
import scala.tools.sbs.regression.StatisticsFactory
import scala.tools.sbs.measurement.PerformanceBenchmark
import scala.tools.sbs.util.Constant

trait TwinningDetector[Detected] {

  protected def log: Log

  protected def config: Config

  def twinningDetect(benchmark: PerformanceBenchmark,
                     measureCurrent: => MeasurementResult,
                     measurePrevious: => MeasurementResult,
                     regressionSuccess: RegressionSuccess => Detected,
                     regressionFailure: RegressionFailure => Detected,
                     measurementFailed: (MeasurementResult, MeasurementResult) => Detected): Detected = {
    UI.info("--Measure current performance")
    log.info("--Measure current performance")
    val current = measureCurrent
    UI.info("--Measure previous performance")
    log.info("--Measure previous performance")
    val previous = measurePrevious

    UI.debug("----Current:  " + current.getClass.getName)
    UI.debug("----Previous: " + previous.getClass.getName)
    log.debug("----Current:  " + current.getClass.getName)
    log.debug("----Previous: " + previous.getClass.getName)

    (current, previous) match {
      case (currentSuccess: MeasurementSuccess, previousSuccess: MeasurementSuccess) => {

        UI.info("[      Run OK      ]")
        log.info("[      Run OK      ]")

        regress(benchmark, currentSuccess, previousSuccess) match {
          case regressSuccess: RegressionSuccess => {

            UI.info("[  Performance OK  ]" + Constant.ENDL)
            log.info("[  Performance OK  ]" + Constant.ENDL)

            regressionSuccess(regressSuccess)
          }
          case regressFailure: RegressionFailure => {

            UI.info("[Performance FAILED]" + Constant.ENDL)
            log.info("[Performance FAILED]" + Constant.ENDL)

            regressionFailure(regressFailure)
          }
        }
      }
      case _ => {

        current match {
          case failure: MeasurementFailure => {
            UI.error("--Measuring current performance failed due to: " + failure.reason)
            log.error("--Measuring current performance failed due to: " + failure.reason)
          }
          case _ => ()
        }
        previous match {
          case failure: MeasurementFailure => {
            UI.error("--Measuring previous performance failed due to: " + failure.reason)
            log.error("--Measuring previous performance failed due to: " + failure.reason)
          }
          case _ => ()
        }

        UI.info("[    Run FAILED    ]" + Constant.ENDL)
        log.info("[    Run FAILED    ]" + Constant.ENDL)

        measurementFailed(current, previous)
      }
    }
  }

  protected def regress(benchmark: PerformanceBenchmark,
                        current: MeasurementSuccess,
                        previous: MeasurementSuccess): RegressionResult = {
    val history = HistoryFactory(log, config, benchmark, Pinpointing)
    history add previous.series
    StatisticsFactory(log) testDifference (benchmark, Pinpointing, current.series, history)
  }

}
