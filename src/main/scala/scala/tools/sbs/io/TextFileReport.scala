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
import scala.tools.sbs.util.Constant.ENDL
import scala.tools.sbs.util.FileUtil

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
      write("Benchmark: " + r.benchmark.name)
      emit(r)
      break
    })
  }

  def emit(result: BenchmarkResult): Unit = result match {
    case success: BenchmarkSuccess => emit(success)
    case ci: ConfidenceIntervalFailure => emit(ci)
    case anova: ANOVAFailure => emit(anova)
    case nope: NoPreviousFailure => emit(nope)
    case imme: ImmeasurableFailure => emit(imme)
    case exp: ExceptionFailure => emit(exp)
    case cpl: CompileFailure => emit(cpl)
  }

  def emit(success: BenchmarkSuccess) {
    write("Mode:      " + success.mode)
    ok()
  }

  def emit(ci: ConfidenceIntervalFailure) {
    write("Mode:      " + ci.mode)
    failed()
    write("         New approach sample mean: " + ci.meansAndSD._1._1.formatted("%.2f") +
      " -SD- " + ci.meansAndSD._1._2.formatted("%.2f"))
    write("         History sample mean:      ")
    write("                                   " + ci.meansAndSD._2._1.formatted("%.2f") +
      " -SD- " + ci.meansAndSD._2._2.formatted("%.2f"))
    write("         Confidence interval:      [" + ci.CI._1.formatted("%.2f") + "; " + ci.CI._2.formatted("%.2f") + "]")
  }

  def emit(anova: ANOVAFailure) {
    write("Mode:      " + anova.mode)
    failed()
    write("         New approach sample mean: " + anova.meansAndSD.last._1.formatted("%.2f") +
      " -SD- " + anova.meansAndSD.last._2.formatted("%.2f"))
    write("         History sample mean:      ")
    anova.meansAndSD.init foreach (m => write("                                   " + m._1.formatted("%.2f") +
      " -SD- " + m._2.formatted("%.2f")))
    write("         F-test:")
    write("                              SSA: " + anova.SSA)
    write("                              SSE: " + anova.SSE)
    write("                           FValue: " + anova.FValue)
    write("                   F distribution: " + anova.F)
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
    write(exp.exception.getStackTraceString)
  }

  def emit(compiless: CompileFailure) {
    failed()
    write("         Compiling benchmark failed")
  }

}
