/*
 * ScrutinyRegressionResult
 * 
 * Version
 * 
 * Created on November 1st, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package pinpoint

import scala.collection.mutable.ArrayBuffer
import scala.tools.sbs.benchmark.Benchmark
import scala.tools.sbs.measurement.MeasurementFailure
import scala.tools.sbs.measurement.MeasurementResult
import scala.tools.sbs.measurement.MeasurementSuccess
import scala.tools.sbs.regression.CIRegressionFailure
import scala.tools.sbs.regression.CIRegressionSuccess
import scala.tools.sbs.regression.ImmeasurableFailure

trait ScrutinyRegressionResult extends ScrutinyResult

class ScrutinyCIRegressionSuccess(_benchmark: Benchmark,
                                  _confidenceLevel: Int,
                                  _current: (Double, Double),
                                  _previous: ArrayBuffer[(Double, Double)],
                                  _CI: (Double, Double),
                                  override val mode: BenchmarkMode = Pinpointing)
  extends CIRegressionSuccess(
    _benchmark,
    Pinpointing,
    _confidenceLevel,
    _current,
    _previous,
    _CI)
  with ScrutinyRegressionResult
  with ScrutinySuccess {

  def this(CISuccess: CIRegressionSuccess) =
    this(
      CISuccess.benchmark,
      CISuccess.confidenceLevel,
      CISuccess.current,
      CISuccess.previous,
      CISuccess.CI)

}

object ScrutinyCIRegressionSuccess {

  def apply(benchmark: Benchmark,
            confidenceLevel: Int,
            current: (Double, Double),
            previous: ArrayBuffer[(Double, Double)],
            CI: (Double, Double)): ScrutinyCIRegressionSuccess =
    new ScrutinyCIRegressionSuccess(benchmark, confidenceLevel, current, previous, CI)

  def apply(CISuccess: CIRegressionSuccess): ScrutinyCIRegressionSuccess =
    new ScrutinyCIRegressionSuccess(CISuccess)

  def unapply(srs: ScrutinyCIRegressionSuccess) =
    if (true) Some(srs.benchmark, srs.confidenceLevel, srs.current, srs.previous, srs.CI)
    else None // Force return type to Option[], 'cause it's too long to be explicitly written :(

}

trait ScrutinyRegressionFailure extends ScrutinyFailure with ScrutinyRegressionResult

class ScrutinyCIRegressionFailure(_benchmark: Benchmark,
                                  _current: (Double, Double),
                                  _previous: ArrayBuffer[(Double, Double)],
                                  _CI: (Double, Double),
                                  override val mode: BenchmarkMode = Pinpointing)
  extends CIRegressionFailure(
    _benchmark,
    Pinpointing,
    _current,
    _previous,
    _CI)
  with ScrutinyRegressionFailure {

  def this(CIFailure: CIRegressionFailure) =
    this(
      CIFailure.benchmark,
      CIFailure.current,
      CIFailure.previous,
      CIFailure.CI)

}

object ScrutinyCIRegressionFailure {

  def apply(benchmark: Benchmark,
            current: (Double, Double),
            previous: ArrayBuffer[(Double, Double)],
            meansAndSD: ArrayBuffer[(Double, Double)],
            CI: (Double, Double)): ScrutinyCIRegressionFailure =
    new ScrutinyCIRegressionFailure(benchmark, current, previous, CI)

  def apply(CISuccess: CIRegressionFailure): ScrutinyCIRegressionFailure =
    new ScrutinyCIRegressionFailure(CISuccess)

  def unapply(srs: ScrutinyCIRegressionFailure) =
    if (true) Some(srs.benchmark, srs.current, srs.previous, srs.CI)
    else None // Force return type to Option[], 'cause it's too long to be explicitly written :(

}

class ScrutinyImmeasurableFailure(_benchmark: Benchmark,
                                  _failure: MeasurementFailure,
                                  override val mode: BenchmarkMode = Pinpointing)
  extends ImmeasurableFailure(
    _benchmark,
    Pinpointing,
    _failure: MeasurementFailure)
  with ScrutinyRegressionFailure {

  def this(imf: ImmeasurableFailure) = this(imf.benchmark, imf.failure)

}

object ScrutinyImmeasurableFailure {

  def apply(benchmark: Benchmark, failure: MeasurementFailure): ScrutinyImmeasurableFailure =
    new ScrutinyImmeasurableFailure(benchmark, failure)

  def apply(imf: ImmeasurableFailure): ScrutinyImmeasurableFailure =
    new ScrutinyImmeasurableFailure(imf)

  def unpply(sif: ScrutinyImmeasurableFailure): Option[(Benchmark, MeasurementFailure)] =
    Some(sif.benchmark, sif.failure)

}
