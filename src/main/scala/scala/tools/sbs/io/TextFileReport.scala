/*
 * TextFileReport
 * 
 * Version
 * 
 * Created on September 18th, 2011
 * 
 * Created by ND P
 */

package scala.tools.sbs
package io

import java.text.SimpleDateFormat
import java.util.Date

import scala.tools.nsc.io.File
import scala.tools.sbs.profiling.ProfilingException
import scala.tools.sbs.regression.ANOVAFailure
import scala.tools.sbs.regression.ConfidenceIntervalFailure
import scala.tools.sbs.regression.ImmeasurableFailure
import scala.tools.sbs.regression.NoPreviousFailure
import scala.tools.sbs.util.Constant.ENDL
import scala.tools.sbs.util.FileUtil

/** An implement of {@link Report}, reporting of a simple text file.
 */
class TextFileReport(config: Config) extends Report {

  val reportFile: File = FileUtil.createFile(config.benchmarkDirectory.path: String, "Report") match {
    case Some(file) => file
    case None => null
  }

  def write(s: String) = FileUtil.write(reportFile.path, s)

  def break() = write("------------------------------------------------------------" + ENDL)

  def ok() = write("Result:---------------------------------------------[  OK  ]")

  def failed() = {
    write("Result:---------------------------------------------[FAILED]")
    write("  Due to:")
  }

  def apply(pack: ResultPack) {
    if (reportFile == null) {
      UI.error("Cannot create report file")
      return
    }

    val date = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm:ss").format(new Date)

    write("Test date:           " + date)
    write("Benchmark directory: " + config.benchmarkDirectory.path)
    write("Total benchmarks:    " + pack.total)
    write("OK:                  " + pack.ok)
    write("Failed:              " + pack.failed)

    break

    pack foreach (r => {
      write("Benchmark: " + r.benchmarkName)
      emit(r)
      break
    })
  }

  def emit(result: BenchmarkResult): Unit = result match {
    case success: BenchmarkSuccess => emit(success.mode)
    case ci: ConfidenceIntervalFailure => emit(ci)
    case anova: ANOVAFailure => emit(anova)
    case nope: NoPreviousFailure => emit(nope)
    case imme: ImmeasurableFailure => emit(imme)
    case exp: ExceptionFailure => emit(exp)
    case cpl: CompileFailure => emit(cpl)
//    case pfe: ProfilingException => emit(pfe)
  }

  def emit(successMode: BenchmarkMode) {
    write("Mode:      " + successMode)
    ok()
  }

  def emit(ci: ConfidenceIntervalFailure) {
    write("Mode:      " + ci.mode)
    failed()
    write("         New approach sample mean: " + ci.meansAndSD._2._1.formatted("%.2f") +
      " -Standard Deviation- " + ci.meansAndSD._2._2.formatted("%.2f"))
    write("         History sample mean:      ")
    write("                                   " + ci.meansAndSD._1._1.formatted("%.2f") +
      " -Standard Deviation- " + ci.meansAndSD._1._2.formatted("%.2f"))
    write("         Confidence interval:      [" + ci.CI._1.formatted("%.2f") + "; " + ci.CI._2.formatted("%.2f") + "]")
  }

  def emit(anova: ANOVAFailure) {
    write("Mode:      " + anova.mode)
    failed()
    write("         New approach sample mean: " + anova.meansAndSD.last._1.formatted("%.2f") +
      " -Standard Deviation- " + anova.meansAndSD.last._2.formatted("%.2f"))
    write("         History sample mean:      ")
    anova.meansAndSD.init foreach (m => write("                                   " + m._1.formatted("%.2f") +
      " -Standard Deviation- " + m._2.formatted("%.2f")))
    write("         F-test:")
    write(" Sum-of-squared due to alternatives: " + anova.SSA)
    write("       Sum-of-squared due to errors: " + anova.SSE)
    write("                       Alternatives: " + "K")
    write("     Each alternatives measurements: " + "N")
    write("SSA * (N - 1) * K / (SSE * (K - 1)): " + anova.FValue)
    write("                     F distribution: " + anova.F)
  }

  def emit(nope: NoPreviousFailure) {
    write("Mode:      " + nope.mode)
    failed()
    write("         No previous measurement result to detect regression")
  }

  def emit(imme: ImmeasurableFailure) {
    write("Mode:      " + imme.mode)
    failed()
    write("         " + imme.measurementFailure.reason)
  }

  def emit(exp: ExceptionFailure) {
    write("Mode:      " + exp.mode)
    failed()
    write("         Exception:                 " + exp.exception.toString)
    write("alksfjdslf")
    write(exp.exception.getStackTraceString)
    write("as;jdflsdkfjsfj")
  }

  def emit(compiless: CompileFailure) {
    failed()
    write("         Compiling benchmark failed")
  }

  def emit(exp: ProfilingException) {
    write("Mode:      " + Profiling)
    failed()
    write("         Exception:                 " + exp.exception.toString)
    write(exp.exception.getStackTraceString)
  }

}
